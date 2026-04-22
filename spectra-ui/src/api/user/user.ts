import { del, get, post, put } from "@/plugin/request/api.ts";

/**
 * 用户相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const userApi = {
    /**
     * 分页获取用户列表
     * @param params 分页参数
     */
    async page(params?: UserPageParams): Promise<Page<UserPageVO>> {
        return get<Page<UserPageVO>>("/api/user/page", params);
    },
    /**
     * 新增用户
     * @param params 角色入参
     */
    async create(params: UserDTO): Promise<void> {
        return post<void>("/api/user", params);
    },
    /**
     * 修改用户
     * @param params 角色入参
     */
    async update(params: UserDTO): Promise<void> {
        return put<void>("/api/user", params);
    },
    /**
     * 删除用户
     * @param id 角色ID
     */
    async deleteById(id: string): Promise<void> {
        return del<void>(`/api/user/${id}`);
    },
    /**
     * 重置用户密码
     * @param id 角色ID
     */
    async passwordResetById(id: string): Promise<void> {
        return put<void>(`/api/user/password/reset/${id}`);
    }
};
