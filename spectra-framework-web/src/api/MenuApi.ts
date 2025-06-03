import http from "@/plugin/request";

export default {
    // 分页获取路由列表
    page(params?: MenuPageParams): Promise<IResult<Page<Menu>>> {
        return http.get<IResult<Page<Menu>>>("/api/menu/page", { params }).then(res => res.data);
    },
    // 获取树形路由
    tree(): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>("/api/menu/tree").then(res => res.data);
    }
};
