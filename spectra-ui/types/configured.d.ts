import type { BasePageParams } from "./paging";

export {};

declare global {
    // 系统配置分页请求参数定义
    type ConfiguredPageParams = BasePageParams & {
        username?: string;
    };

    // 系统配置信息
    type ConfiguredPageVO = {
        // 主键ID
        id: string;
        // 系统配置 键
        key: string;
        // 系统配置 值
        value: any;
        // 系统配置类型
        type: string;
        // 如果为字典则是字典组CODE
        dict_code: string;
        // 备注
        remarks: string;
    };

    // 系统配置信息 表单编辑
    type ConfiguredForm = {
        // 主键ID
        id: string;
        // 系统配置 键
        key: string;
        // 系统配置 值
        value: any;
        // 系统配置类型
        type: string;
        // 如果为字典则是字典组CODE
        dict_code: string;
        // 备注
        remarks: string;
    };

    // 系统配置信息 请求提交
    type ConfiguredDTO = {
        // 主键ID
        id: string;
        // 系统配置 键
        key: string;
        // 系统配置 值
        value: any;
        // 系统配置类型
        type: string;
        // 如果为字典则是字典组CODE
        dict_code: string;
        // 备注
        remarks: string;
    };
}
