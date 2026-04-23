export {};

declare global {
    // 系统配置信息
    type Configured = BaseEntity & {
        key: string;
        value: any;
        type: string;
        dict_code: string;
        remarks: string;
    };
}
