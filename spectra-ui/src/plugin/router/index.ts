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

import { createRouter, createWebHashHistory } from "vue-router";
import useUserStore from "@/plugin/store/modules/useUserStore";
import useAppStore from "@/plugin/store/modules/useAppStore.ts";
import { hideLoading, showLoading } from "@/plugin/element/loading";
import routes from "@/plugin/router/routes";
import { getRouteTitle, loadMenu } from "@/utils/RouteUtils.ts";

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
    const userStore = useUserStore();
    const appStore = useAppStore();
    const token = userStore.token;
    const menus = appStore.menus;

    console.debug(`[路由守卫] 开始 | token: ${!!token.access_token}, 目标: ${to.path}`);

    // 1. 白名单：直接放行
    if (whiteList.has(to.path)) {
        console.log("[守卫] 白名单通过");
        showLoading();
        return next();
    }

    // 2. 无 token：跳转登录
    if (!token.access_token) {
        console.log("[守卫] 无 token，跳转登录页");
        hideLoading();
        return next({ path: "/login" });
    }

    // 3. 有 token 但访问登录页：重定向到主页
    if (to.path === "/login") {
        console.log("[守卫] 有 token 但访问登录页，重定向到主页");
        return next({ path: "/" });
    }

    // 4. 需要加载菜单（首次进入或刷新）
    if (menus.length === 0 || sessionStorage.getItem("reloaded")) {
        console.log("[守卫] 需要加载菜单");
        await loadMenu(router, to, next);
    }

    // 5. 路由未匹配（404）
    if (to.matched.length === 0) {
        console.log("[守卫] 路由未匹配，跳转 404");
        hideLoading();
        return next({ path: "/404" });
    }

    // 6. 正常放行
    console.log("[守卫] 正常跳转");
    showLoading();
    next();
});

// 路由后置守卫
router.afterEach(to => {
    const title = getRouteTitle(to.meta.title);
    document.title = title
        ? `${import.meta.env.VITE_WEB_TITLE} - ${title}`
        : import.meta.env.VITE_WEB_TITLE;
    hideLoading();
});


export default router;
