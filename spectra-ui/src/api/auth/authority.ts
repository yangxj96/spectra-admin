import { del, get, post, put } from "@/plugin/request/api";

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
    create(params: RolePageVO): Promise<void> {
        return post<void>("/api/authority", params);
    },
    /**
     * 删除权限
     * @param id 权限ID
     */
    delete(id: string): Promise<void> {
        return del<void>("/api/authority/" + id);
    },
    /**
     * 修改权限
     * @param params 权限入参
     */
    update(params: RolePageVO): Promise<void> {
        return put<void>("/api/authority", params);
    },
    /**
     * 树形权限列表
     */
    tree(): Promise<AuthorityTree[]> {
        return get<AuthorityTree[]>("/api/authority/tree");
    }
};
