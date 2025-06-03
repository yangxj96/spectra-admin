import http from "@/plugin/request";

export default {
    // 获取树形路由
    async tree(): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>("/api/menu/tree").then(res => res.data);
    }
};
