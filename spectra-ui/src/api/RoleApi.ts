import http from "@/plugin/request";
import type { TreeKey } from "element-plus";

export default {
    // 创建角色
    async created(params: Role) {
        return http.post("/api/role", params).then(res => res.data);
    },
    async delete(id: string): Promise<IResult<any>> {
        return http.delete("/api/role/" + id).then(res => res.data);
    },
    // 修改角色
    async modify(params: Role) {
        return http.put("/api/role", params).then(res => res.data);
    },
    // 分页查询
    async page(params?: RolePageParams): Promise<IResult<Page<Role>>> {
        return http
            .get<IResult<Page<Role>>>("/api/role/page", {
                params
            })
            .then(res => res.data);
    },
    // 列表查询
    async list(): Promise<IResult<Role[]>> {
        return http.get<IResult<Role[]>>("/api/role/list").then(res => res.data);
    },
    // 获取当前角色下有哪些权限
    async getRoleAuthority(roleId: string): Promise<IResult<Authority[]>> {
        return http.get<IResult<Authority[]>>(`/api/role/${roleId}/authority`).then(res => res.data);
    },
    // 获取当前角色下有哪些菜单
    async getRoleMenu(roleId: string): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>(`/api/role/${roleId}/menu`).then(res => res.data);
    },
    // 获取当前角色下有哪些权限
    async saveRoleAuthority(params: { role_id: string; authority_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.post<IResult>(`/api/role/${params.role_id}/authority`, params).then(res => res.data);
    },
    // 获取当前角色下有哪些菜单
    async saveRoleMenu(params: { role_id: string; menu_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.post<IResult>(`/api/role/${params.role_id}/menu`, params).then(res => res.data);
    }
};
