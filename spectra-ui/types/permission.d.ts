export {};

declare global {
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

    // 角色分页查询VO
    type RolePageVO = {
        // 主键ID
        id: string;
        //角色名称
        name: string;
        // 角色代码
        code: string;
        //角色状态
        state: boolean;
        //角色范围
        scope: number;
        // 自定义范围ID列表
        target_ids: string[];
        // 是否内置
        builtin: boolean;
        //角色备注
        remark: string;
    };

    // 角色编辑表单类型
    type RoleForm = {
        // 主键ID
        id: string;
        // 角色名称
        name: string;
        // 角色代码
        code: string;
        // 角色状态
        state: boolean | undefined;
        // 角色范围
        scope: number | undefined;
        // 自定义范围ID列表
        target_ids: string[];
        // 是否内置
        builtin: boolean | undefined;
        // 角色备注
        remark: string;
    };

    // 角色新增修改请求类型
    type RoleDTO = {
        // 主键ID
        id: string;
        //角色名称
        name: string;
        // 角色代码
        code: string;
        //角色状态
        state: boolean | undefined;
        //角色范围
        scope: number | undefined;
        // 自定义范围ID列表
        target_ids: string[];
        // 是否内置
        builtin: boolean | undefined;
        //角色备注
        remark: string;
    };
}
