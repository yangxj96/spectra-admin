import http from "@/plugin/request";

export default {
    // 获取组织机构树形列表
    async tree() {
        return await http.get<IResult<OrganizationTree[]>>("/api/organization/tree").then(res => res.data);
    },
    // 新增组织机构
    async created(params: Organization): Promise<IResult> {
        return await http.post<IResult>("/api/organization", params).then(res => res.data);
    },
    // 根据ID删除组织机构
    async deleteById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/organization/${id}`).then(res => res.data);
    },
    // 修改组织机构
    async modify(params: Organization): Promise<IResult> {
        return http.put<IResult>("/api/organization", params).then(res => res.data);
    }
};
