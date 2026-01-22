import http from "@/plugin/request";

export const configuredApi = {
    // 分页查询系统配置信息
    async page() {
        return http.get("/api/configured/page").then(res => res.data);
    },
    // 修改系统配置
    async modify(params: Configured) {
        return await http.put<IResult>("/api/configured", params).then(res => res.data);
    },
    // 修改系统配置
    async json() {
        return await http.get<IResult>("/api/configured/json").then(res => res.data);
    }
};
