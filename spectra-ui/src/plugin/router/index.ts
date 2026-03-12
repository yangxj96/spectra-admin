import { createRouter, createWebHashHistory } from "vue-router";

import { hideLoading, showLoading } from "@/plugin/element/loading";
import { cancelAllRequests } from "@/plugin/request/http.ts";
import routes from "@/plugin/router/routes";
import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";
import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";
import { getRouteTitle, loadMenu } from "@/utils/route-utils.ts";

const router = createRouter({
    history: createWebHashHistory(),
    routes,
    scrollBehavior() {
        return {
            top: 0
        };
    }
});

const whiteList = new Set(["/login"]);

// 路由前置守卫
router.beforeEach(async (to, _, next) => {
    // 路由切换时取消所有未完成请求
    cancelAllRequests();

    const userStore = useUserStore();
    const appStore = useAppStore();
    const token = userStore.token;
    const menus = appStore.menus;

    console.debug(`[路由守卫] 开始 | token: ${!!token.access_token}, 目标: ${to.path}`);

    // 1. 白名单：直接放行
    if (whiteList.has(to.path)) {
        console.debug("[守卫] 白名单通过");
        showLoading();
        return next();
    }

    // 2. 无 token：跳转登录
    if (!token.access_token) {
        console.debug("[守卫] 无 token，跳转登录页");
        hideLoading();
        return next({ path: "/login" });
    }

    // 3. 有 token 但访问登录页：重定向到主页
    if (to.path === "/login") {
        console.debug("[守卫] 有 token 但访问登录页，重定向到主页");
        return next({ path: "/" });
    }

    // 4. 需要加载菜单（首次进入或刷新）
    if (menus.length === 0 || sessionStorage.getItem("reloaded")) {
        console.debug("[守卫] 需要加载菜单");
        return await loadMenu(router, to, next);
    }

    // 5. 路由未匹配（404）
    if (to.matched.length === 0) {
        console.debug("[守卫] 路由未匹配，跳转 404");
        hideLoading();
        return next({ path: "/404" });
    }

    // 6. 正常放行
    console.debug("[守卫] 正常跳转");
    showLoading();
    next();
});

// 路由后置守卫
router.afterEach(to => {
    const title = getRouteTitle(to.meta.title);
    document.title = title ? `${import.meta.env.VITE_WEB_TITLE} - ${title}` : import.meta.env.VITE_WEB_TITLE;
    hideLoading();
});

export default router;
