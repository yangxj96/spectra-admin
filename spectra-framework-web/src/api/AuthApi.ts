import http from "@/plugin/request";

export default {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     */
    async login(username: string, password: string, code: string) {
        const response = await http.post<IResult<Token>>("/api/auth/login", {
            username: username,
            password: password,
            code: code
        });
        return response.data;
    },
    /**
     * 退出登录
     */
    async logout() {
        const resp = await http.post("/api/auth/logout");
        return resp.data;
    }
};
