import router from "@/plugin/router/index";

/**
 * 全局工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export default {
    /**
     * 退出程序需要的处理内容
     */
    exit() {
        globalThis.localStorage.clear();
        globalThis.sessionStorage.clear();
        location.reload();
    },
    toLogin() {
        // 安全获取当前路径
        const currentRoute = router.currentRoute.value;
        let fromPath = currentRoute.fullPath || "/";
        if (currentRoute.fullPath.includes("/login")) {
            fromPath = "/";
        }

        try {
            // 清除认证数据
            globalThis.localStorage.clear();
            globalThis.sessionStorage.clear();

            // 关键：使用 fromPath，而不是再次读取 currentRoute
            router
                .push({
                    path: "/login",
                    query: {
                        redirect: fromPath
                    }
                })
                .then(() => {
                    console.log(`用户已登出，跳转至登录页，来源: ${fromPath}`);
                });
        } catch (error) {
            console.error("登出过程发生错误:", error);
            // 确保无论如何都跳转
            globalThis.location.href = "/login";
        }
    }
};
