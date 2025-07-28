import http from "@/plugin/request";

export default {
    // 获取树形路由
    async tree(): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>("/api/menu/tree").then(res => res.data);
    },
    // 新增菜单
    async created(params: Menu) {
        return http.post<IResult<Menu>>("/api/menu/created", params).then(res => res.data);
    },
    // 修改菜单
    async modify(params: Menu) {
        return http.put<IResult<Menu>>("/api/menu/modify", params).then(res => res.data);
    }
};
