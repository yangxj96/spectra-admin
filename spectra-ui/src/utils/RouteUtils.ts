/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import { defineAsyncComponent } from "@vue/runtime-core";
import type { NavigationGuardNext, RouteLocationNormalizedLoadedGeneric, Router, RouteRecordRaw } from "vue-router";
import useAppStore from "@/plugin/store/modules/useAppStore.ts";
import MenuApi from "@/api/MenuApi.ts";
import { ElMessage } from "element-plus";
import { hideLoading } from "@/plugin/element/loading.ts";

/**
 * 加载组件 在views文件夹下面,且扩展名需要是vue
 * @param componentPath 组件路径
 */
const loadComponent = (componentPath?: string): ReturnType<typeof defineAsyncComponent> | undefined => {
    if (!componentPath) return undefined;
    const normalizedPath = componentPath.startsWith("/") ? componentPath.slice(1) : componentPath;
    return () => import(/* @vite-ignore */ `/src/views/${normalizedPath}.vue`);
};

/**
 * menus数组转换成路由数组对象
 * @param menus 菜单数组
 */
export const convertMenuToRoutes = (menus: Menu[]): RouteRecordRaw[] => {
    return menus.map(menu => {
        const route: RouteRecordRaw = {
            path: menu.path,
            name: menu.name,
            component: menu.layout
                ? () => import(/* @vite-ignore */ `@/components/Layout/index.vue`)
                : loadComponent(menu.component),
            meta: {
                title: menu.name
            },
            children: []
        };
        // 根据菜单是否有下级,进行转换
        if (menu.children && menu.children.length > 0) {
            route.redirect = menu.children[0].path;
            route.children = convertMenuToRoutes(menu.children);
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
        const res = await MenuApi.tree();
        if (res.code === 200 && res.data) {
            appStore.menus = res.data;
            const routes = convertMenuToRoutes(res.data);

            // ✅ 优化：避免重复添加路由
            for (const route of routes) {
                if (!router.hasRoute(route.name!)) {
                    router.addRoute(route);
                    console.log(`[守卫] 添加路由: ${String(route.name)}`);
                }
            }

            console.log(`[守卫] 动态添加 ${routes.length} 个路由`);

            // ✅ vue-router 4.5.1：确保路由表已更新
            // 虽然 still need hack，但更安全
            return next({ ...to, replace: true });
        } else {
            ElMessage.error("获取菜单失败");
            console.warn("[守卫] 获取菜单失败，跳转登录");
            hideLoading();
            return next({ path: "/login" });
        }
    } catch (error) {
        console.error("[守卫] 加载菜单时发生异常", error);
        ElMessage.error("网络异常，获取菜单失败");
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

    if (title != undefined && typeof title === "object") {
        console.warn("[getRouteTitle] Title is an object, may stringify to [object Object]:", title);
    }

    return "";
}
