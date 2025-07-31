use std::io::{Cursor, Read};
use reqwest::{Client, header::{HeaderMap, USER_AGENT}};
use zip::read::ZipArchive;

use crate::structs::Story;
use crate::error::{Result, WattpadError};

/// A dedicated client for the Wattpad API.
pub struct WattpadClient {
    client: Client,
}

impl WattpadClient {
    /// Creates a new WattpadClient with a default reqwest::Client.
    /// This client is configured to handle cookies for authentication.
    pub fn new() -> Result<Self> {
        let mut headers = HeaderMap::new();
        headers.insert(
            USER_AGENT,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36"
                .parse()
                .unwrap(),
        );

        let client = Client::builder()
            .cookie_store(true)
            .default_headers(headers)
            .build()?;

        Ok(WattpadClient { client })
    }

    /// Creates a new WattpadClient with a pre-existing reqwest::Client.
    /// This is useful for setting custom headers, like a User-Agent.
    /// NOTE: For authentication to work, the provided client must have a cookie store enabled.
    pub fn new_with_client(client: Client) -> Self {
        WattpadClient { client }
    }

    // --- Auth Endpoint ---

    /// Logs in to Wattpad to get authentication cookies for private requests.
    /// The cookies are stored in the client and used for all subsequent requests.
        pub async fn login(&self, username: &str, password: &str) -> Result<()> {
        let url = "https://www.wattpad.com/auth/login?&_data=routes%2Fauth.login";
        let params = [("username", username), ("password", password)];

        let response = self.client.post(url).form(&params).send().await?;

        // The Python script's logic: check if the response contains any cookies.
        // A successful login will always include `Set-Cookie` headers.
        // `response.cookies()` returns an iterator over the cookies from the response.
        if response.cookies().next().is_none() {
            // No cookies were returned, so the login failed.
            return Err(WattpadError::AuthenticationFailed(
                "Login failed. The server did not return any cookies. Check credentials.".to_string(),
            ));
        }

        // If we received cookies, we still check for any other potential HTTP errors.
        response.error_for_status()?;

        // The login is successful. The cookies have been automatically saved to the client's cookie store.
        Ok(())
    }

    // --- Story Endpoints ---

    /// Retrieves the raw text content of a story's parts (chapters).
    pub async fn retrieve_story_content(&self, story_id: i32) -> Result<Vec<String>> {
        let resp = self.client
            .get("https://www.wattpad.com/apiv2/?m=storytext")
            .query(&[("group_id", story_id.to_string()), ("output", "zip".to_string())])
            .send()
            .await?
            .bytes()
            .await?;

        let mut archive = ZipArchive::new(Cursor::new(resp))?;
        let mut parts = Vec::with_capacity(archive.len());

        for i in 0..archive.len() {
            let mut file = archive.by_index(i)?;
            let mut content = String::new();
            file.read_to_string(&mut content)?;
            parts.push(content);
        }

        Ok(parts)
    }

    /// Retrieves detailed information about a single story.
    pub async fn retrieve_story_info(&self, story_id: i32) -> Result<Story> {
        let fields = "id,title,modifyDate,language(name),user(username),description,parts(id,title,modifyDate),cover";
        let url = format!("https://www.wattpad.com/api/v3/stories/{}", story_id);
        
        let story = self.client
            .get(url)
            .query(&[("fields", fields)])
            .send()
            .await?
            .json::<Story>()
            .await?;
        
        Ok(story)
    }
}
