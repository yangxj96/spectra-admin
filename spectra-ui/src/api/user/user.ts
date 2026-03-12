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
    async page(params?: UserPageParams): Promise<IResult<Page<User>>> {
        return get<IResult<Page<User>>>("/api/user/page", params);
    },
    /**
     * 新增用户
     * @param params 角色入参
     */
    async created(params: User) {
        return post<IResult>("/api/user", params);
    },
    /**
     * 修改用户
     * @param params 角色入参
     */
    async modify(params: User) {
        return put<IResult>("/api/user", params);
    },
    /**
     * 删除用户
     * @param id 角色ID
     */
    async deleteById(id: string) {
        return del<IResult>(`/api/user/${id}`);
    },
    /**
     * 重置用户密码
     * @param id 角色ID
     */
    async passwordResetById(id: string) {
        return put<IResult>(`/api/user/password/reset/${id}`);
    },
    /**
     * 获取所有在线用户
     */
    async online(): Promise<IResult> {
        return get<IResult>("/api/user/online");
    }
};
