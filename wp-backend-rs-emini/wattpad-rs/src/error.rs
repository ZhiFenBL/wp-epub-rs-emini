use thiserror::Error;

#[derive(Error, Debug)]
pub enum WattpadError {
    #[error("A network or request error occurred")]
    Request(#[from] reqwest::Error),

    #[error("Failed to parse zip archive")]
    Zip(#[from] zip::result::ZipError),

    #[error("An IO error occurred")]
    Io(#[from] std::io::Error),

    // for specific login failures
    #[error("Authentication failed: {0}")]
    AuthenticationFailed(String),
}

pub type Result<T> = std::result::Result<T, WattpadError>;
