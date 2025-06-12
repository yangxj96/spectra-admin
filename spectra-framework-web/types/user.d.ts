// 登录token
type Token = {
    id: string;
    /** 用户名 */
    username: string;
    /** 认证token */
    access_token: string;
    /** 权限列表 */
    authorities: string[];
    /** 角色 */
    roles: string[];
};

// 用户
type User = BaseEntity & {
    username: string;
    tel: string;
    email: string;
    dept: string;
    status: boolean;
    role: string;
};
