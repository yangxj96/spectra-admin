import { del, get, post, put } from "@/plugin/request/api";

import type { TreeKey } from "element-plus";

/**
 * 角色相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const roleApi = {
    /**
     * 创建角色
     * @param params 角色入参
     */
    create(params: RoleForm): Promise<void> {
        return post<void>("/api/role", params);
    },
    /**
     * 根据ID删除角色
     * @param id 角色ID
     */
    delete(id: string): Promise<void> {
        return del<void>("/api/role/" + id);
    },
    /**
     * 修改角色
     * @param params 角色入参
     */
    update(params: RoleForm): Promise<void> {
        return put<void>("/api/role", params);
    },
    /**
     * 分页查询
     * @param params 分页参数
     */
    page(params?: RolePageParams): Promise<Page<RolePageVO>> {
        return get<Page<RolePageVO>>("/api/role/page", params);
    },
    /**
     * 列表查询（全量）
     */
    list(): Promise<RolePageVO[]> {
        return get<RolePageVO[]>("/api/role/list");
    },
    /**
     * 根据角色ID获取角色下有哪些权限
     * @param roleId 角色ID
     */
    getRoleAuthority(roleId: string): Promise<Authority[]> {
        return get<Authority[]>(`/api/role/${roleId}/authority`);
    },
    /**
     * 根据角色ID获取角色下有哪些菜单
     * @param roleId 角色ID
     */
    getRoleMenu(roleId: string): Promise<Menu[]> {
        return get<Menu[]>(`/api/role/${roleId}/menu`);
    },
    /**
     * 关联角色-权限(全量)
     * @param params 角色ID和权限列表
     */
    saveRoleAuthority(params: { role_id: string; authority_ids: TreeKey[] | undefined }): Promise<void> {
        return put<void>(`/api/role/${params.role_id}/authorities`, params);
    },
    /**
     * 管理角色-菜单
     * @param params 角色ID和菜单列表
     */
    saveRoleMenu(params: { role_id: string; menu_ids: TreeKey[] | undefined }): Promise<void> {
        return put<void>(`/api/role/${params.role_id}/menus`, params);
    }
};
