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
    // 名称
    name: string;
    // 邮箱
    email: string;
    // 状态
    state: number;
    // 角色列表
    roles: Role[];
    // 角色ID列表
    role_ids: string[];
    // 组织机构ID
    organization_id: string;
};
