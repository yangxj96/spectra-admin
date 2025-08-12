import { createRouter, createWebHashHistory } from "vue-router";
import useUserStore from "@/plugin/store/modules/useUserStore";
import useAppStore from "@/plugin/store/modules/useAppStore.ts";
import { hideLoading, showLoading } from "@/plugin/element/loading";
import routes from "@/plugin/router/routes";
import MenuApi from "@/api/MenuApi.ts";
import { convertMenuToRoutes } from "@/utils/RouteUtils.ts";
import { ElMessage } from "element-plus";

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
    // 获取token
    const token = useUserStore().token;
    console.debug(`[路由守卫 - 前置] - 开始 token:${token.access_token},目标:${to.path}`);
    // 检测是否是白名单
    if (whiteList.has(to.path)) {
        console.log("白名单通过");
        next();
    }
    // token不存在,跳转到登录页
    else if (token.access_token === undefined) {
        console.log("token不存在,跳转到登录页");
        // 不存在token 则跳转到登录页
        next({ path: "/login" });
    }
    // 有token,但是前往的是登录页,要让他跳转到主页
    else if (token.access_token && to.path === "/login") {
        console.log("有token,但是前往的是登录页,要让他跳转到主页");
        next({ path: "/" });
    }
    // 检测是否有菜单
    else if (useAppStore().menus.length === 0 || sessionStorage.getItem("reloaded")) {
        // 移除标记
        sessionStorage.removeItem("reloaded");
        let menus = useAppStore().menus;
        // 判断是否有菜单信息,没有则需要拉去菜单信息
        if (menus.length === 0) {
            let res = await MenuApi.tree();
            if (res.code == 200 && res.data) {
                useAppStore().menus = res.data;
                menus = res.data;
            } else {
                ElMessage.success({
                    message: "获取菜单失败"
                });
                next({ path: "/login" });
            }
        }
        let routers = convertMenuToRoutes(menus);
        for (const item of routers) {
            router.addRoute(item);
        }
        // hack方法 确保addRoutes已完成
        next({ ...to, replace: true });
    }
    // 路径不存在则跳转到404
    else if (to.matched.length <= 0) {
        console.log("路径不存在则跳转到404");
        hideLoading();
        next({ path: "/404" });
    }
    // 正常跳转
    else {
        console.log("正常跳转");
        showLoading();
        next();
    }
    console.debug("[路由守卫 - 前置] - 解析守卫结束");
});

// 路由后置守卫
router.afterEach(to => {
    // 切换标题
    if (to.meta.title) {
        // 强制转换成了string,后续优化
        document.title = import.meta.env.VITE_WEB_TITLE + " - " + String(to.meta.title);
    }
    hideLoading();
});

declare module "vue-router" {
    interface RouteMeta {
        /** 修改的标题 */
        title?: string;
    }
}

export default router;
