import http from "@/plugin/request";
import useUserStore from "@/plugin/store/modules/useUserStore";

export default {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     */
    async login(username: string, password: string) {
        const response = await http.post<IResult<Token>>("/api/auth/login", {
            username: username,
            password: password
        });
        return response.data;
    },
    /**
     * 退出登录
     */
    async logout() {
        const resp = await http.post("/api/auth/logout", {
            token: useUserStore().token.access_token
        });
        return resp.data;
    }
};
