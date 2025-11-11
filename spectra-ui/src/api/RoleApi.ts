import http from "@/plugin/request";
import type { TreeKey } from "element-plus";

/**
 * 角色相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export default {
    /**
     * 创建角色
     * @param params 角色入参
     */
    async created(params: Role) {
        return http.post("/api/role", params).then(res => res.data);
    },
    /**
     * 根据ID删除角色
     * @param id 角色ID
     */
    async delete(id: string): Promise<IResult> {
        return http.delete("/api/role/" + id).then(res => res.data);
    },
    /**
     * 修改角色
     * @param params 角色入参
     */
    async modify(params: Role) {
        return http.put("/api/role", params).then(res => res.data);
    },
    /**
     * 分页查询
     * @param params 分页参数
     */
    async page(params?: RolePageParams): Promise<IResult<Page<Role>>> {
        return http
            .get<IResult<Page<Role>>>("/api/role/page", {
                params
            })
            .then(res => res.data);
    },
    /**
     * 列表查询（全量）
     */
    async list(): Promise<IResult<Role[]>> {
        return http.get<IResult<Role[]>>("/api/role/list").then(res => res.data);
    },
    /**
     * 根据角色ID获取角色下有哪些权限
     * @param roleId 角色ID
     */
    async getRoleAuthority(roleId: string): Promise<IResult<Authority[]>> {
        return http.get<IResult<Authority[]>>(`/api/role/${roleId}/authority`).then(res => res.data);
    },
    /**
     * 根据角色ID获取角色下有哪些菜单
     * @param roleId 角色ID
     */
    async getRoleMenu(roleId: string): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>(`/api/role/${roleId}/menu`).then(res => res.data);
    },
    /**
     * 关联角色-权限(全量)
     * @param params 角色ID和权限列表
     */
    async saveRoleAuthority(params: { role_id: string; authority_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.put<IResult>(`/api/role/${params.role_id}/authorities`, params).then(res => res.data);
    },
    /**
     * 管理角色-菜单
     * @param params 角色ID和菜单列表
     */
    async saveRoleMenu(params: { role_id: string; menu_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.put<IResult>(`/api/role/${params.role_id}/menus`, params).then(res => res.data);
    }
};
