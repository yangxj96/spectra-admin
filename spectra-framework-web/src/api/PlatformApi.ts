import http from "@/plugin/request";

export default {
    // 分页获取路由列表
    async page(params?: RoutePageParams): Promise<IResult<Page<Route>>> {
        return http.get<IResult<Page<Route>>>("/api/platform/route/page", { params }).then(res => res.data);
    }
};
