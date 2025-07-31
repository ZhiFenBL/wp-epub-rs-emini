pub mod error;
pub mod structs;
pub mod wattpad_api;

// Re-export the main client and structs for easier access.
pub use error::{Result, WattpadError};
pub use structs::{Story, Part, Lang, WithID, WithUsername};
pub use wattpad_api::WattpadClient;
