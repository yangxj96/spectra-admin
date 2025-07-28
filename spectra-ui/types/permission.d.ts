// 权限
type Authority = BaseEntity & {
    // 父ID
    pid?: string;
    // 权限名称
    name: string;
    // 权限编码
    code: string;
};

// 权限树形
type AuthorityTree = Authority & {
    // 下级权限
    children: AuthorityTree[];
};

// 角色
type Role = BaseEntity & {
    //角色名称
    name: string;
    //角色状态
    state: boolean;
    //角色范围
    scope: number;
    //角色备注
    remark: string;
};
