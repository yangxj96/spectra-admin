import http from "@/plugin/request";

export default {
    // 创建角色
    async createdRole(params: Role) {
        return http.post("/api/permission/createdRole", params).then(res => res.data);
    },
    // 修改角色
    async modifyRole(params: Role) {
        return http.put("/api/permission/modifyRole", params).then(res => res.data);
    },
    // 分页查询
    async pageRole(params?: RolePageParams): Promise<IResult<Page<Role>>> {
        return http
            .get<IResult<Page<Role>>>("/api/permission/pageRole", {
                params
            })
            .then(res => res.data);
    }
};
