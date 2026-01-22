import http from "@/plugin/request";

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
    async tree(): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>("/api/menu/tree").then(res => res.data);
    },
    /**
     * 新增菜单
     * @param params 菜单入参
     */
    async created(params: Menu) {
        return http.post<IResult<Menu>>("/api/menu/created", params).then(res => res.data);
    },
    /**
     * 修改菜单
     * @param params 菜单入参
     */
    async modify(params: Menu) {
        return http.put<IResult<Menu>>("/api/menu/modify", params).then(res => res.data);
    }
};
