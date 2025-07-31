// build.rs
fn main() {
    uniffi::generate_scaffolding("./src/wp_epub_mini.udl").unwrap();
}