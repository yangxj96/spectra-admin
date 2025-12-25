import http from "@/plugin/request";

export default {
    // 分页查询系统配置信息
    async page() {
        return http.get("/api/configured/page").then(res => res.data);
    }
};
