use crate::platform::PlatformSpecific;

pub struct Linux;

impl PlatformSpecific for Linux {
    fn platform_specific_function(&self) -> String {
        "Unknown".to_string();
    }
}
