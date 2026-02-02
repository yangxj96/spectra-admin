export {};

declare global {
    type LoginFrom = {
        type: "PASSWORD" | "SMS" | "SCAN" | "WECHAT" | "GITHUB";
        username: string;
        password: string;
        clientId: string;
        captcha: string;
    };

    // 登录token
    type Token = {
        id: string;
        // 用户名
        username: string;
        // 认证token
        access_token: string;
        // 权限列表
        authorities: string[];
        // 角色
        roles: Role[];
    };

    // 用户
    type User = BaseEntity & {
        // 姓名
        username: string;
        // 真实姓名
        real_name: string;
        // 状态
        status: number;
        // 性别
        gender: number;
        // 生日
        birthday: Date;
        // 手机号码
        phone: string;
        // 邮箱
        email: string;
        // 国家
        country: string;
        // 城市
        city: string;
        // 语言
        language: string;
        // 时区
        timezone: string;
        // 角色列表
        roles: Role[];
        // 角色ID列表
        role_ids: string[];
        // 组织机构ID
        department_id: string;
        // 数据范围
        data_scope: string;
        // 数据范围为自定义的情况下的目标组织机构ID
        target_ids: number[];
    };
}
