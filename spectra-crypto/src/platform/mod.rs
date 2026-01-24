#[cfg(target_os = "windows")]
pub mod windows;

#[cfg(target_os = "linux")]
pub mod linux;

#[cfg(target_os = "macos")]
pub mod macos;

pub trait PlatformSpecific {
    fn platform_specific_function(&self)-> String;
}

#[cfg(target_os = "windows")]
pub fn platform_specific() -> Box<dyn PlatformSpecific> {
    Box::new(windows::Windows {})
}

#[cfg(target_os = "linux")]
pub fn platform_specific() -> Box<dyn PlatformSpecific> {
    Box::new(linux::Linux {})
}

#[cfg(target_os = "macos")]
pub fn platform_specific() -> Box<dyn PlatformSpecific> {
    Box::new(macos::MacOS {})
}