export {};

declare global {
    // CPU信息
    type CPUInfo = {
        name: string;
        load: string;
        vendor: string;
        family: string;
        model: string;
        stepping: string;
        identifier: string;
        is64bit: boolean;
        physical_cores: number;
        logical_cores: number;
        max_frequency_hz: string;
        max_frequency_ghz: string;
    };

    // CPU信息
    type RAMInfo = {
        summary: string;
        count: string;
        total_capacity_bytes: string;
        total_capacity_gb: string;
        slots: RAMSlotInfo[];
    };

    // CPU信息(单条)
    type RAMSlotInfo = {
        slot: number;
        memory_type: string;
        clock_speed_hz: string;
        clock_speed_mhz: string;
        capacity_bytes: string;
        capacity_gb: string;
    };

    // JVM信息
    type JVMInfo = {
        jvm_name: string;
        jvm_vendor: string;
        jvm_version: string;
        jvm_spec_name;
        jvm_spec_version: string;
        jvm_spec_vendor: string;
        java_version: string;
        java_home: string;
        java_vendor: string;
        java_vendor_url: string;
        start_time: string;
        pid: string;
        process_id: string;
        jvm_arguments: string[];
        system_properties: Map<string, string>;
        class_path: string;
        library_path: string;
    };
}
