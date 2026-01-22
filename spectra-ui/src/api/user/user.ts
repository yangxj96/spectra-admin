import http from "@/plugin/request/index.ts";

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
        return http.get<IResult<Page<User>>>("/api/user/page", { params }).then(res => res.data);
    },
    /**
     * 新增用户
     * @param params 角色入参
     */
    async created(params: User) {
        return http.post<IResult>("/api/user", params).then(res => res.data);
    },
    /**
     * 修改用户
     * @param params 角色入参
     */
    async modify(params: User) {
        return http.put<IResult>("/api/user", params).then(res => res.data);
    },
    /**
     * 修改用户
     * @param id 角色ID
     */
    async deleteById(id: string) {
        return http.delete<IResult>(`/api/user/${id}`).then(res => res.data);
    },
    /**
     * 重置用户密码
     * @param id 角色ID
     */
    async passwordResetById(id: string) {
        return http.put<IResult>(`/api/user/password/reset/${id}`).then(res => res.data);
    },
    /**
     * 获取所有在线用户
     */
    async online(): Promise<IResult> {
        return http.get<IResult>("/api/user/online").then(res => res.data);
    }
};
