import router from "@/plugin/router/index";

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
        // 🛡️ 安全获取当前路径
        const currentRoute = router.currentRoute.value;
        const fromPath = currentRoute.fullPath || "/"; // fallback 到首页

        try {
            // 清除认证数据
            globalThis.localStorage.clear();
            globalThis.sessionStorage.clear();

            // 👇 关键：使用 fromPath，而不是再次读取 currentRoute
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
