use serde::Deserialize;
use wmi::{COMLibrary, WMIConnection};
use crate::platform::PlatformSpecific;

pub struct Windows;

#[derive(Deserialize, Debug)]
#[serde(rename = "Win32_Processor")]
#[serde(rename_all = "PascalCase")]
pub struct Processor {
    processor_id: Option<String>, // CPU 序列号
}

#[derive(Deserialize, Debug)]
#[serde(rename = "Win32_PhysicalMemory")]
#[serde(rename_all = "PascalCase")]
pub struct Memory {
    serial_number: Option<String>, // 内存条序列号
}

#[derive(Deserialize, Debug)]
#[serde(rename = "Win32_BaseBoard")]
#[serde(rename_all = "PascalCase")]
pub struct Motherboard {
    serial_number: Option<String>, // 主板序列号
}

impl PlatformSpecific for Windows {
    fn platform_specific_function(&self)-> String {
        let com_lib = COMLibrary::new().unwrap();
        let wmi = WMIConnection::new(com_lib.into()).unwrap();

        // 查询CPU序列号
        let cpu_info: Vec<Processor> = wmi.query().unwrap();
        let cpu_serial = cpu_info
            .get(0)
            .and_then(|cpu| cpu.processor_id.clone())
            .unwrap_or_else(|| "Unknown CPU".to_string());

        println!("CPU序列号: {}", cpu_serial);

        // 查询内存条序列号
        let memory_info: Vec<Memory> = wmi.query().unwrap();
        let memory_serials: Vec<String> = memory_info
            .into_iter()
            .filter_map(|mem| mem.serial_number)
            .collect();
        let memory_serial = if memory_serials.is_empty() {
            "Unknown Memory".to_string()
        } else {
            memory_serials.join("_") // 如果有多个内存条，使用 `_` 连接
        };

        println!("内存条序列号: {}", memory_serial);

        // 查询主板序列号
        let motherboard_info: Vec<Motherboard> = wmi.query().unwrap();
        let motherboard_serial = motherboard_info
            .get(0)
            .and_then(|board| board.serial_number.clone())
            .unwrap_or_else(|| "Unknown Motherboard".to_string());

        println!("主板序列号: {}", motherboard_serial);

        // 拼接硬件信息
        format!("{}{}{}", cpu_serial, memory_serial, motherboard_serial)
    }
}
