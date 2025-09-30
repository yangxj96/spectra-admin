import http from "@/plugin/request";

export default {
    // 创建权限
    async created(params: Role) {
        return http.post("/api/authority", params).then(res => res.data);
    },
    // 删除权限
    async delete(id: string) {
        return http.delete("/api/authority/" + id).then(res => res.data);
    },
    // 修改权限
    async modify(params: Role) {
        return http.put("/api/authority", params).then(res => res.data);
    },
    // 树形权限列表
    async tree(): Promise<IResult<AuthorityTree[]>> {
        return http.get<IResult<AuthorityTree[]>>("/api/authority/tree").then(res => res.data);
    }
};
