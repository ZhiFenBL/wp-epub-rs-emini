use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use serde_with::{serde_as, DisplayFromStr};

// -----------------------------------------------------------------------------
// Language
// -----------------------------------------------------------------------------

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct Lang {
    name: String,
}

impl Lang {
    pub fn name(&self) -> &str {
        &self.name
    }
}

// -----------------------------------------------------------------------------
// Minimal Models (WithUsername, WithID)
// -----------------------------------------------------------------------------

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct WithUsername {
    username: String,
}

impl WithUsername {
    pub fn username(&self) -> &str {
        &self.username
    }
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct WithID {
    id: i32,
}

impl WithID {
    pub fn id(&self) -> i32 {
        self.id
    }
}

// -----------------------------------------------------------------------------
// Part Model
// -----------------------------------------------------------------------------

#[serde_as]
#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct Part {
    id: i32,
    title: String,
    modify_date: Option<DateTime<Utc>>,
}

impl Part {
    pub fn id(&self) -> i32 { self.id }
    pub fn title(&self) -> &str { &self.title }
    pub fn modify_date(&self) -> Option<&DateTime<Utc>> { self.modify_date.as_ref() }
}

// -----------------------------------------------------------------------------
// Story Model
// -----------------------------------------------------------------------------

#[serde_as]
#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct Story {
    #[serde_as(deserialize_as = "DisplayFromStr")]
    id: i32,
    title: String,
    cover: String,
    user: WithUsername,
    language: Lang,
    description: String,
    parts: Vec<Part>,
    modify_date: DateTime<Utc>,
}

impl Story {
    pub fn id(&self) -> i32 { self.id }
    pub fn title(&self) -> &str { &self.title }
    pub fn cover(&self) -> &str { &self.cover }
    pub fn user(&self) -> &WithUsername { &self.user }
    pub fn language(&self) -> &Lang { &self.language }
    pub fn description(&self) -> &str { &self.description }
    pub fn parts(&self) -> &Vec<Part> { &self.parts }
    pub fn modify_date(&self) -> &DateTime<Utc> { &self.modify_date }
}