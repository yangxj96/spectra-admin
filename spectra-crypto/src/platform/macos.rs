use crate::platform::PlatformSpecific;
use std::process::Command;

pub struct MacOS;


impl PlatformSpecific for MacOS {
    fn platform_specific_function(&self)-> String {
        // Fetch CPU serial number (MacBook model identifier)
        let cpu_serial = Command::new("sysctl")
            .arg("machdep.cpu.serial")
            .output()
            .map(|output| String::from_utf8_lossy(&output.stdout).trim().to_string())
            .unwrap_or_else(|_| "Unknown CPU".to_string());

        // Fetch memory information (using `sysctl` or `system_profiler`)
        let memory_info = Command::new("system_profiler")
            .arg("SPMemoryDataType")
            .output()
            .map(|output| String::from_utf8_lossy(&output.stdout).to_string())
            .unwrap_or_else(|_| "Unknown Memory".to_string());

        // Fetch motherboard info (using `system_profiler`)
        let motherboard_info = Command::new("system_profiler")
            .arg("SPHardwareDataType")
            .output()
            .map(|output| String::from_utf8_lossy(&output.stdout).to_string())
            .unwrap_or_else(|_| "Unknown Motherboard".to_string());

        // You may want to parse the system_profiler output more specifically depending on needs.
        // For simplicity, we're returning raw results.

        println!("CPU序列号: {}", cpu_serial);
        println!("内存条信息: {}", memory_info);
        println!("主板信息: {}", motherboard_info);

        // Returning concatenated hardware info
        format!("{} {} {}", cpu_serial, memory_info, motherboard_info)
    }
}
