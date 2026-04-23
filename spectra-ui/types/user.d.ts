export {};

declare global {
    type LoginForm = {
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
        roles: RolePageVO[];
    };

    // 用户分页查询的实体
    type UserPageVO = {
        // 主键
        id: string;
        // 用户名
        username: string;
        // 头像
        avatar: string;
        // 状态
        status: number;
        // 真实姓名
        real_name: string;
        // 性别
        gender: number;
        // 生日
        birthday: string;
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
        roles: RolePageVO[];
        // 部门ID
        department_id: string;
        // 部门名称
        department_name: string;
        // 数据范围类型
        data_scope: number;
        // 目标ID列表
        target_ids: string[];
        // 创建时间
        created_at: string;
    };

    // 用户表单相关使用的
    type UserForm = {
        // 主键
        id: string;
        // 用户名
        username: string;
        // 真实姓名
        real_name: string;
        // 状态
        status: number | undefined;
        // 性别
        gender: number | undefined;
        // 生日
        birthday: string;
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
        // 部门ID
        department_id: string;
        // 角色ID列表
        role_ids: string[];
        // 数据范围类型
        data_scope: number | undefined;
        // 目标ID列表
        target_ids: string[];
    };

    // 用户表单提交内容
    type UserDTO = {
        // 主键
        id: string;
        // 用户名
        username: string;
        // 真实姓名
        real_name: string;
        // 状态
        status: number | undefined;
        // 性别
        gender: number | undefined;
        // 生日
        birthday: string;
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
        // 部门ID
        department_id: string;
        // 角色ID列表
        role_ids: string[];
        // 数据范围类型
        data_scope: number | undefined;
        // 目标ID列表
        target_ids: string[];
    };
}
