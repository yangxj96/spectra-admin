import { del, get, post, put } from "@/plugin/request/api.ts";

/**
 * 菜单相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const menuApi = {
    /**
     * 获取树形路由
     */
    tree(): Promise<Menu[]> {
        return get<Menu[]>("/api/menu/tree");
    },
    /**
     * 新增菜单
     * @param params 菜单入参
     */
    create(params: Menu): Promise<void> {
        return post<void>("/api/menu/created", params);
    },
    /**
     * 修改菜单
     * @param params 菜单入参
     */
    update(params: Menu): Promise<void> {
        return put<void>("/api/menu/modify", params);
    },
    /**
     * 删除菜单
     * @param id 菜单ID
     */
    deleteById(id: string): Promise<void> {
        return del<void>(`/api/menu/${id}`);
    }
};
