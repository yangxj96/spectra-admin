export {};

declare global {
    // 菜单元数据
    type MenuMetadata = {
        // 已知
        title?: string;
        // 字段兜底
        [key: string]: unknown;
    };

    // 菜单
    type Menu = BaseEntity & {
        //父级ID
        pid: string;
        //图标
        icon: string;
        //名称
        name: string;
        //路径
        path: string;
        //组件地址
        component: string;
        //排序
        sort: number;
        //布局
        layout?: string;
        //模块
        module?: string;
        //参数
        params?: never;
        //元数据
        meta?: never;
        // 是否显示菜单
        hide?: boolean;
        // 元数据
        metadata?: MenuMetadata | JsonValue;
        //子级
        children?: Menu[];
    };

    // 部门
    type Department = BaseEntity & {
        // 上级ID
        pid: string;
        // 名称
        name: string;
        // 代码
        code: string;
        // 类型
        type: number;
        // 路径
        path: string;
        // 备注
        remark?: string;
    };

    // 组织机构树形
    type DepartmentTree = Department & {
        // 子级
        children?: DepartmentTree[];
    };

    // 字典组
    type DictGroup = BaseEntity & {
        //父级ID
        pid: string;
        //字典类型名称
        name: string;
        //字典类型编码
        code: string;
        //状态
        state: number;
        //备注
        remark?: string;
        //是否内置
        builtin?: boolean;
        //是否隐藏
        hide?: boolean;
    };

    // 字典组树形结构
    type DictTypeTree = DictGroup & {
        // 下级内容
        children?: DictTypeTree[];
    };

    // 字典数据
    type DictItem = BaseEntity & {
        // 字典组ID
        gid: string;
        // 字典标签
        label: string;
        // 字典值
        value: string;
        // 排序
        sort: number;
        // 状态
        state: number;
        // 是否默认
        default_flag: boolean;
        // 备注
        remark?: string;
    };

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

    // 系统配置信息
    type Configured = BaseEntity & {
        key: string;
        value: any;
        type: string;
        dict_code: string;
        remarks: string;
    };
}
