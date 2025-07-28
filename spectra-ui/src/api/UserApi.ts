import http from "@/plugin/request/index.ts";

export default {
    // 分页获取用户列表
    async page(params?: UserPageParams): Promise<IResult<Page<User>>> {
        return http.get<IResult<Page<User>>>("/api/user/page", { params }).then(res => res.data);
    },
    // 新增用户
    async created(params: User) {
        return http.post<IResult>("/api/user", params).then(res => res.data);
    },
    // 修改用户
    async modify(params: User) {
        return http.put<IResult>("/api/user", params).then(res => res.data);
    },
    // 修改用户
    async deleteById(id: string) {
        return http.delete<IResult>(`/api/user/${id}`).then(res => res.data);
    }
};
