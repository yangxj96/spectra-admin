import { type AsyncComponentLoader, type defineAsyncComponent } from "vue";

import { menuApi } from "@/api/system/menu.ts";
import { hideLoading } from "@/plugin/element/loading.ts";
import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import type { NavigationGuardNext, RouteLocationNormalizedLoadedGeneric, Router, RouteRecordRaw } from "vue-router";

// 自动收集所有 views 下的 vue 文件（构建期完成）
const viewModules = import.meta.glob("/src/views/**/*.vue");

/**
 * 加载组件 在views文件夹下面,且扩展名需要是vue
 * @param componentPath 组件路径
 */
const loadComponent = (componentPath?: string): ReturnType<typeof defineAsyncComponent> | undefined => {
    if (!componentPath) return undefined;

    // 统一路径格式
    const normalizedPath = componentPath.replace(/^\/+/, "").replace(/\.vue$/, "");

    const fullPath = `/src/views/${normalizedPath}.vue`;

    const loader = viewModules[fullPath];

    if (!loader) {
        console.error(`[loadComponent] 组件未找到: ${fullPath}`, Object.keys(viewModules));
        return undefined;
    }

    return loader as AsyncComponentLoader;
};

// 动态加载布局组件
const loadLayout = (layoutName: string) => {
    const layouts: Record<string, () => Promise<unknown>> = {
        default: () => import("@/components/Layouts/Default/index.vue"),
        blank: () => import("@/components/Layouts/Blank/index.vue")
    };
    return layouts[layoutName] || layouts["default"];
};

/**
 * menus数组转换成路由数组对象
 * @param menus 菜单数组
 */
export const convertMenuToRoutes = (menus: Menu[]): RouteRecordRaw[] => {
    return menus.map(menu => {
        // 确保 menu.path 以 / 开头
        const path = menu.path.startsWith("/") ? menu.path : "/" + menu.path;
        const meta = {
            title: menu.name,
            ...(typeof menu.metadata === "object" && menu.metadata !== null
                ? (menu.metadata as Record<string, JsonValue>)
                : {})
        };
        const route: RouteRecordRaw = {
            path: menu.path,
            name: menu.name,
            component: menu.layout ? loadLayout(menu.layout.trim().toLowerCase()) : loadComponent(menu.component),
            meta,
            children: []
        };

        // 处理有子菜单的情况
        if (menu.children && menu.children.length > 0) {
            // 生成子路由
            const childRoutes = convertMenuToRoutes(menu.children);

            // 计算第一个子路由的完整路径
            const firstChildPath = childRoutes[0]?.path;
            if (firstChildPath) {
                // 拼接成绝对路径：/system + /user → /system/user
                // 注意：如果 firstChildPath 是相对路径（如 'user'），需要拼接
                route.redirect = path.endsWith("/")
                    ? path + firstChildPath.replace(/^\//, "")
                    : path + "/" + firstChildPath.replace(/^\//, ""); // 设置 redirect 为完整路径
            }

            route.children = childRoutes;
        }

        return route;
    });
};

/**
 * 加载路由
 * @param router 路由
 * @param to 目标
 * @param next 进入下一步
 */
export const loadMenu = async (router: Router, to: RouteLocationNormalizedLoadedGeneric, next: NavigationGuardNext) => {
    const appStore = useAppStore();

    // 防止重复请求
    if (appStore.isFetchingMenus) {
        console.log("[守卫] 菜单正在加载中，跳过");
        return next(); // 可等待，或使用 Promise 缓存
    }

    sessionStorage.removeItem("reloaded");
    appStore.isFetchingMenus = true; // 设置加载状态

    try {
        const menus = await menuApi.tree();
        // if (res.code === 200 && res.data) {
        // 不使用 as 会出现 TS2589,暂时没搞定
        appStore.menus = menus as never;
        const routes = convertMenuToRoutes(menus);

        // 避免重复添加路由
        for (const route of routes) {
            if (!router.hasRoute(route.name!)) {
                router.addRoute(route);
            }
        }

        // 确保路由表已更新 虽然 still need hack，但更安全
        return next({ ...to, replace: true });
    } catch (error) {
        console.error("[守卫] 加载菜单时发生异常", error);
        MessageUtils.error("网络异常，获取菜单失败");
        hideLoading();
        return next({ path: "/login" });
    } finally {
        appStore.isFetchingMenus = false;
    }
};

/**
 * 安全获取路由标题，防止 [object Object] 或异常
 */
export function getRouteTitle(title: unknown): string {
    if (typeof title === "string") {
        return title;
    }

    if (typeof title === "function") {
        try {
            const result = title();
            return typeof result === "string" ? result : "";
        } catch (error) {
            console.error("[getRouteTitle] Title function execution failed:", error);
            return "";
        }
    }

    if (title !== undefined && typeof title === "object") {
        console.warn("[getRouteTitle] Title is an object, may stringify to [object Object]:", title);
    }

    return "";
}
