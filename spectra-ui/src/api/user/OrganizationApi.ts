import http from "@/plugin/request";

/**
 * 组织机构相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export default {
    /**
     * 获取组织机构树形列表
     */
    async tree() {
        return await http.get<IResult<OrganizationTree[]>>("/api/organization/tree").then(res => res.data);
    },
    /**
     * 新增组织机构
     * @param params 组织机构入参
     */
    async created(params: Organization): Promise<IResult> {
        return await http.post<IResult>("/api/organization", params).then(res => res.data);
    },
    /**
     * 根据ID删除组织机构
     * @param id 组织机构ID
     */
    async deleteById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/organization/${id}`).then(res => res.data);
    },
    /**
     * 修改组织机构
     * @param params 组织机构入参
     */
    async modify(params: Organization): Promise<IResult> {
        return http.put<IResult>("/api/organization", params).then(res => res.data);
    }
};
