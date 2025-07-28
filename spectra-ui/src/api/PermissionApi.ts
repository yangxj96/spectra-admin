import http from "@/plugin/request";

export default {
    // 创建角色
    async createdRole(params: Role) {
        return http.post("/api/permission/role", params).then(res => res.data);
    },
    // 修改角色
    async modifyRole(params: Role) {
        return http.put("/api/permission/role", params).then(res => res.data);
    },
    // 分页查询
    async pageRole(params?: RolePageParams): Promise<IResult<Page<Role>>> {
        return http
            .get<IResult<Page<Role>>>("/api/permission/role/page", {
                params
            })
            .then(res => res.data);
    },
    // 列表查询
    async listRole(): Promise<IResult<Role[]>> {
        return http.get<IResult<Role[]>>("/api/permission/role/list").then(res => res.data);
    },
    // 权限树查询
    async authorityTree(): Promise<IResult<AuthorityTree[]>> {
        return http.get<IResult<AuthorityTree[]>>("/api/permission/authority/tree").then(res => res.data);
    }
};
