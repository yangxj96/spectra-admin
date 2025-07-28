import http from "@/plugin/request";

export default {
    // 获取组织机构树形列表
    async tree() {
        return await http.get<IResult<OrganizationTree[]>>("/api/organization/tree").then(res => res.data);
    },
    // 新增组织机构
    async created(params: Organization) {
        return await http.post<IResult>("/api/organization", params).then(res => res.data);
    },
    // 修改组织机构
    async modify(params: Organization) {
        return http.put<IResult<Menu>>("/api/organization", params).then(res => res.data);
    }
};
