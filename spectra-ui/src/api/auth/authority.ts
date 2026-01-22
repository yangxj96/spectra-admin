import http from "@/plugin/request";

/**
 * 权限相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const authorityApi = {
    /**
     * 创建权限
     * @param params 权限入参
     */
    async created(params: Role) {
        return http.post("/api/authority", params).then(res => res.data);
    },
    /**
     * 删除权限
     * @param id 权限ID
     */
    async delete(id: string) {
        return http.delete("/api/authority/" + id).then(res => res.data);
    },
    /**
     * 修改权限
     * @param params 权限入参
     */
    async modify(params: Role) {
        return http.put("/api/authority", params).then(res => res.data);
    },
    /**
     * 树形权限列表
     */
    async tree(): Promise<IResult<AuthorityTree[]>> {
        return http.get<IResult<AuthorityTree[]>>("/api/authority/tree").then(res => res.data);
    }
};
