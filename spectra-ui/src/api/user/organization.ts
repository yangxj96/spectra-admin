import http from "@/plugin/request";

/**
 * 组织机构相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const departmentApi = {
    /**
     * 获取组织机构树形列表
     */
    async tree() {
        return await http.get<IResult<DepartmentTree[]>>("/api/department/tree").then(res => res.data);
    },
    /**
     * 新增组织机构
     * @param params 组织机构入参
     */
    async created(params: Department): Promise<IResult> {
        return await http.post<IResult>("/api/department", params).then(res => res.data);
    },
    /**
     * 根据ID删除组织机构
     * @param id 组织机构ID
     */
    async deleteById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/department/${id}`).then(res => res.data);
    },
    /**
     * 修改组织机构
     * @param params 组织机构入参
     */
    async modify(params: Department): Promise<IResult> {
        return http.put<IResult>("/api/department", params).then(res => res.data);
    }
};
