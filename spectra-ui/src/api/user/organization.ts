import { del, get, post, put } from "@/plugin/request/api.ts";

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
    tree(): Promise<DepartmentTreeVO[]> {
        return get<DepartmentTreeVO[]>("/api/department/tree");
    },
    /**
     * 新增组织机构
     * @param params 组织机构入参
     */
    create(params: DepartmentDTO): Promise<void> {
        return post<void>("/api/department", params);
    },
    /**
     * 根据ID删除组织机构
     * @param id 组织机构ID
     */
    deleteById(id: string): Promise<void> {
        return del<void>(`/api/department/${id}`);
    },
    /**
     * 修改组织机构
     * @param params 组织机构入参
     */
    update(params: DepartmentDTO): Promise<void> {
        return put<void>("/api/department", params);
    }
};
