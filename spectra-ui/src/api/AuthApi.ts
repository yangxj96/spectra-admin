import http from "@/plugin/request";

export default {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     */
    async login(username: string, password: string, code: string) {
        return await http
            .post<IResult<Token>>("/api/auth/login", {
                username: username,
                password: password,
                code: code
            })
            .then(response => response.data);
    },
    // 退出登录
    async logout() {
        return await http.post("/api/auth/logout").then(response => response.data);
    },
    // 检查token是否还能用
    async check() {
        return await http.post("/api/auth/check").then(response => response.data);
    }
};
