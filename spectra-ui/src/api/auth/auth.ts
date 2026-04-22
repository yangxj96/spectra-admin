import { post } from "@/plugin/request/api";

/**
 * 认证授权相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const authApi = {
    /**
     * 用户登录
     */
    login(form: LoginForm): Promise<Token> {
        return post<Token>("/api/auth/login", form, {
            priority: "high",
            fetchPriority: "high"
        });
    },
    /**
     * 退出登录
     */
    logout(): Promise<void> {
        return post<void>("/api/auth/logout");
    },
    /**
     * 检查token是否还能用
     */
    check(): Promise<void> {
        return post<void>("/api/auth/logout");
    }
};
