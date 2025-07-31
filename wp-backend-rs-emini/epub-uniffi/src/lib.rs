use anyhow::{anyhow, Context, Result};
use futures::stream::{self, StreamExt};
use iepub::prelude::{EpubBuilder, EpubHtml};
use lol_html::{element, html_content::ContentType, HtmlRewriter, Settings};
use once_cell::sync::Lazy;
use quick_xml::events::Event;
use quick_xml::Reader;
use quick_xml::Writer;
use reqwest::Client;
use std::collections::HashMap;
use std::io::Cursor;
use std::sync::{Arc, Mutex};
use tokio::runtime::Runtime;
use wattpad_rs::WattpadClient;

// --- STATE MANAGEMENT ---

// A dedicated, global async runtime to execute our tasks.
static RUNTIME: Lazy<Runtime> = Lazy::new(|| {
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .expect("Failed to build Tokio runtime")
});

// A single, shared, lazily-initialized reqwest client.
// This client's cookie store will persist across FFI calls.
static LAZY_CLIENT: Lazy<Client> = Lazy::new(|| {
    Client::builder()
        .cookie_store(true) // Enable cookies
        .user_agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
        .build()
        .expect("Failed to build shared reqwest client")
});

// Internal struct to hold the processed data for each chapter
struct ProcessedChapter {
    index: usize,
    title: String,
    file_name: String,
    html_content: String,
    images: Vec<ImageAsset>,
}

// Internal struct to hold image data
struct ImageAsset {
    epub_path: String,
    data: Vec<u8>,
}

/// Downloads a Wattpad story and compiles it into an EPUB file in memory.
/// This is the internal "business logic" function.
pub async fn download_wattpad_story_as_epub(
    // It now accepts the shared client
    reqwest_client: &Client,
    story_id: i32,
    embed_images: bool,
    concurrent_requests: usize,
    output_path: &str,
) -> Result<String> {
    // --- 1. Setup ---
    // Use the provided shared client to create a WattpadClient instance
    let client = WattpadClient::new_with_client(reqwest_client.clone());

    // --- 2. Fetch Story Info ---
    let story = client
        .retrieve_story_info(story_id)
        .await
        .context(format!("Failed to fetch info for story ID {}", story_id))?;

    // --- 3. Fetch Content (Metadata + HTML) Concurrently ---
    let chapter_contents = client.retrieve_story_content(story_id).await?;
    let chapter_metadata = story.parts();

    // --- 4. Process Chapters Concurrently ---
    let chapters_to_process = chapter_metadata
        .iter()
        .cloned()
        .zip(chapter_contents.into_iter());

    let processed_chapters: Vec<Result<ProcessedChapter>> =
        stream::iter(chapters_to_process.enumerate())
            .map(|(i, (metadata, html_content))| {
                let client_clone = reqwest_client.clone();
                async move {
                    process_chapter(
                        &client_clone,
                        i + 1,
                        metadata.title(),
                        &html_content,
                        embed_images,
                        concurrent_requests,
                    )
                    .await
                }
            })
            .buffer_unordered(concurrent_requests)
            .collect()
            .await;

    let mut successfully_processed: Vec<ProcessedChapter> = processed_chapters
        .into_iter()
        .filter_map(Result::ok)
        .collect();

    successfully_processed.sort_by_key(|c| c.index);

    // --- 5. Build EPUB ---
    let mut epub_builder = EpubBuilder::default()
        .with_title(escape_xml_chars(story.title()))
        .with_creator(escape_xml_chars(story.user().username()))
        .with_description(escape_xml_chars(story.description()));

    if let Ok(Some(cover_data)) = download_image(reqwest_client, story.cover()).await {
        epub_builder = epub_builder.cover("cover.jpg", cover_data);
    }

    for chapter in successfully_processed {
        for image in chapter.images {
            epub_builder = epub_builder.add_assets(&image.epub_path, image.data);
        }
        epub_builder = epub_builder.add_chapter(
            EpubHtml::default()
                .with_title(&chapter.title)
                .with_file_name(&chapter.file_name)
                .with_data(chapter.html_content.as_bytes().to_vec()),
        );
    }

    // --- 6. Generate EPUB in memory ---
    let sanitized_title = sanitize_filename(story.title());
    epub_builder
        .file(output_path)
        .map_err(|e| anyhow!("Failed to generate EPUB file: {:?}", e))?;

    Ok(sanitized_title)
}

// --- Private Helper Function ---

fn escape_xml_chars(s: &str) -> String {
    s.replace('&', "&amp;")
     .replace('<', "&lt;")
     .replace('>', "&gt;")
     .replace('"', "&quot;")
     .replace('\'', "&apos;")
}

fn re_encode_html(html_fragment: &str) -> Result<String> {
    let wrapped_html = format!("<root>{}</root>", html_fragment);
    let mut reader = Reader::from_str(&wrapped_html);
    let config = reader.config_mut();
    config.trim_text(true);
    config.expand_empty_elements = false;
    let mut writer = Writer::new(Cursor::new(Vec::new()));
    loop {
        match reader.read_event() {
            Ok(Event::Start(e)) if e.name().as_ref() != b"root" => {
                writer.write_event(Event::Start(e))?;
            }
            Ok(Event::End(e)) if e.name().as_ref() != b"root" => {
                writer.write_event(Event::End(e))?;
            }
            Ok(_event @ Event::Start(_)) | Ok(_event @ Event::End(_)) => {}
            Ok(Event::Eof) => break,
            Ok(e) => {
                writer.write_event(e)?;
            }
            Err(e) => {
                return Err(anyhow::anyhow!(
                    "XML parsing error at position {}: {:?}",
                    reader.buffer_position(),
                    e
                ));
            }
        }
    }
    let result_bytes = writer.into_inner().into_inner();
    let final_string = String::from_utf8(result_bytes)?;
    Ok(final_string)
}

async fn process_chapter(
    client: &Client,
    index: usize,
    title: &str,
    html_in: &str,
    embed_images: bool,
    concurrent_requests: usize,
) -> Result<ProcessedChapter> {
    let mut images = Vec::new();
    let output_buffer = Arc::new(Mutex::new(String::new()));
    let mut image_map = HashMap::new();

    if embed_images {
        let image_urls = collect_image_urls(html_in)?;
        let image_futures = stream::iter(image_urls)
            .map(|url| async {
                let image_data = download_image(client, &url).await?;
                Ok((url, image_data))
            })
            .buffer_unordered(concurrent_requests)
            .collect::<Vec<Result<(String, Option<Vec<u8>>)>>>()
            .await;

        for (img_idx, result) in image_futures.into_iter().flatten().enumerate() {
            let (url, data_option) = result;
            if let Some(data) = data_option {
                let extension = infer_extension_from_data(&data).unwrap_or("jpg");
                let epub_path = format!("images/chapter_{}/image_{}.{}", index, img_idx, extension);
                image_map.insert(url, epub_path.clone());
                images.push(ImageAsset { epub_path, data });
            }
        }
    }

    let output_clone = Arc::clone(&output_buffer);
    let mut rewriter = HtmlRewriter::new(
        Settings {
            element_content_handlers: vec![
                element!("p[data-media-type='image']", |el| {
                    el.remove_and_keep_content();
                    Ok(())
                }),
                element!("*[data-p-id]", |el| {
                    el.remove_attribute("data-p-id");
                    Ok(())
                }),
                element!("br", |el| {
                    el.replace("<br />", ContentType::Html);
                    Ok(())
                }),
                element!("img", move |el| {
                    let mut new_tag = String::from("<img");
                    if let Some(src) = el.get_attribute("src") {
                        let new_src = if embed_images {
                            image_map.get(&src).cloned().unwrap_or(src)
                        } else {
                            src
                        };
                        new_tag.push_str(&format!(" src=\"{}\"", new_src));
                    }
                    if let Some(alt) = el.get_attribute("alt") {
                        new_tag.push_str(&format!(" alt=\"{}\"", alt));
                    }
                    if let Some(style) = el.get_attribute("style") {
                        new_tag.push_str(&format!(" style=\"{}\"", style));
                    }
                    new_tag.push_str(" />");
                    el.replace(&new_tag, ContentType::Html);
                    Ok(())
                }),
            ],
            ..Settings::default()
        },
        |c: &[u8]| {
            output_clone
                .lock()
                .unwrap()
                .push_str(&String::from_utf8_lossy(c));
        },
    );

    rewriter.write(html_in.as_bytes())?;
    rewriter.end()?;
    let cleaned_html = output_buffer.lock().unwrap().clone();
    let correctly_encoded_html =
        re_encode_html(&cleaned_html).context("Failed to re-encode HTML for XML compatibility")?;

    Ok(ProcessedChapter {
        index,
        title: escape_xml_chars(title),
        file_name: format!("{}.xhtml", index),
        html_content: correctly_encoded_html,
        images,
    })
}

async fn download_image(client: &Client, url: &str) -> Result<Option<Vec<u8>>> {
    if url.is_empty() { return Ok(None); }
    let response = client.get(url).send().await;
    match response {
        Ok(resp) if resp.status().is_success() => Ok(Some(resp.bytes().await?.to_vec())),
        _ => Ok(None),
    }
}

fn collect_image_urls(html: &str) -> Result<Vec<String>> {
    let urls = Arc::new(Mutex::new(Vec::new()));
    let urls_clone = Arc::clone(&urls);
    let mut rewriter = HtmlRewriter::new(
        Settings {
            element_content_handlers: vec![element!("img[src]", move |el| {
                if let Some(src) = el.get_attribute("src") { urls_clone.lock().unwrap().push(src); }
                Ok(())
            })],
            ..Settings::default()
        },
        |_: &[u8]| {},
    );
    rewriter.write(html.as_bytes())?;
    rewriter.end()?;
    Ok(Arc::try_unwrap(urls).unwrap().into_inner().unwrap())
}

fn infer_extension_from_data(data: &[u8]) -> Option<&str> {
    match data {
        [0xFF, 0xD8, 0xFF, ..] => Some("jpg"),
        [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, ..] => Some("png"),
        [0x47, 0x49, 0x46, 0x38, ..] => Some("gif"),
        _ => None,
    }
}

fn sanitize_filename(name: &str) -> String {
    name.chars().map(|c| match c {
        '<' | '>' | ':' | '"' | '/' | '\\' | '|' | '?' | '*' => '_',
        _ => c,
    }).collect()
}

// --- UniFFI Additions ---

#[derive(Debug, thiserror::Error)]
pub enum EpubError {
    #[error("Download failed: {reason}")]
    Download { reason: String },
    
    // auth error variant
    #[error("Authentication failed: {reason}")]
    Authentication { reason: String },
}

// NOTE: We don't implement a generic From<anyhow::Error> (anymore)
// to be more explicit about which error type we're creating.

pub struct EpubResult {
    pub title: String,
    pub output_path: String,
}

/// Exposed FFI function to log in to Wattpad.
pub async fn login(username: String, password: String) -> Result<(), EpubError> {
    // Spawn the async task onto our dedicated runtime
    let handle = RUNTIME.spawn(async move {
        let client = WattpadClient::new_with_client(LAZY_CLIENT.clone());
        client.login(&username, &password).await
    });

    // Await the result of the spawned task and map errors
    match handle.await {
        Ok(Ok(_)) => Ok(()), // Success
        Ok(Err(e)) => Err(EpubError::Authentication { reason: e.to_string() }), // Wattpad-rs error
        Err(join_error) => Err(EpubError::Authentication { reason: format!("Internal task failed: {}", join_error) }), // Tokio runtime error
    }
}

/// Exposed FFI function to download a story.
pub async fn download_wattpad_story(
    story_id: i32,
    embed_images: bool,
    concurrent_requests: u64,
    output_path: String,
) -> Result<EpubResult, EpubError> {
    // Clone path for moving into the async block
    let path_clone = output_path.clone();

    // Spawn the async task onto our dedicated runtime
    let handle = RUNTIME.spawn(async move {
        download_wattpad_story_as_epub(
            &LAZY_CLIENT,
            story_id,
            embed_images,
            concurrent_requests as usize,
            &path_clone,
        ).await
    });

    // Await the result of the spawned task and map errors
    match handle.await {
        Ok(Ok(title)) => Ok(EpubResult { title, output_path }), // Success
        Ok(Err(e)) => Err(EpubError::Download { reason: e.to_string() }), // Internal anyhow error
        Err(join_error) => Err(EpubError::Download { reason: format!("Internal task failed: {}", join_error) }), // Tokio runtime error
    }
}

uniffi::include_scaffolding!("wp_epub_mini");
