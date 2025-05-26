import http from "@/plugin/request/index.ts";

export default {
    // 分页获取用户列表
    page(params?: UserPageParams): Promise<IResult<Page<User>>> {
        return http.get<IResult<Page<User>>>("/api/user/page", { params }).then(res => res.data);
    }
};
