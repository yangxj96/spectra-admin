import http from "@/plugin/request";
import type { TreeKey } from "element-plus";

export default {
    // 创建角色
    async createdRole(params: Role) {
        return http.post("/api/permission/role", params).then(res => res.data);
    },
    // 修改角色
    async modifyRole(params: Role) {
        return http.put("/api/permission/role", params).then(res => res.data);
    },
    // 分页查询
    async pageRole(params?: RolePageParams): Promise<IResult<Page<Role>>> {
        return http
            .get<IResult<Page<Role>>>("/api/permission/role/page", {
                params
            })
            .then(res => res.data);
    },
    // 列表查询
    async listRole(): Promise<IResult<Role[]>> {
        return http.get<IResult<Role[]>>("/api/permission/role/list").then(res => res.data);
    },
    // 权限树查询
    async authorityTree(): Promise<IResult<AuthorityTree[]>> {
        return http.get<IResult<AuthorityTree[]>>("/api/permission/authority/tree").then(res => res.data);
    },
    // 获取当前角色下有哪些权限
    async getRoleAuthority(roleId: string): Promise<IResult<Authority[]>> {
        return http.get<IResult<Authority[]>>(`/api/permission/role/${roleId}/authority`).then(res => res.data);
    },
    // 获取当前角色下有哪些菜单
    async getRoleMenu(roleId: string): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>(`/api/permission/role/${roleId}/menu`).then(res => res.data);
    },
    // 获取当前角色下有哪些权限
    async saveRoleAuthority(params: { role_id: string; authority_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.post<IResult>(`/api/permission/role/${params.role_id}/authority`, params).then(res => res.data);
    },
    // 获取当前角色下有哪些菜单
    async saveRoleMenu(params: { role_id: string; menu_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.post<IResult>(`/api/permission/role/${params.role_id}/menu`, params).then(res => res.data);
    }
};
