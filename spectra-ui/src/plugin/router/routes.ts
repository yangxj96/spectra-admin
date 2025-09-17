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

import { type RouteRecordRaw } from "vue-router";

const layout = () => import("@/components/Layout/index.vue");

/**
 * 通用的路由,所有人都有的
 */
export default [
    {
        path: "/login",
        name: "login",
        component: () => import("@/views/Login/index.vue"),
        meta: {
            title: "登录"
        }
    },
    {
        path: "/404",
        name: "no_matching",
        component: () => import("@/views/Common/404/index.vue"),
        meta: {
            title: "未匹配到页面"
        }
    },
    {
        path: "/401",
        name: "no_access",
        component: () => import("@/views/Common/401/index.vue"),
        meta: {
            title: "无权访问"
        }
    },
    {
        path: "/redirect/:path*",
        name: "redirect",
        component: () => import("@/views/Common/Redirect/index.vue"),
        meta: {
            title: "返回原来页面"
        }
    },
    {
        path: "",
        name: "home",
        component: layout,
        redirect: "",
        meta: {
            title: "首页"
        },
        children: [
            {
                path: "",
                name: "首页",
                component: () => import("@/views/Home/index.vue"),
                meta: {
                    title: "首页"
                }
            }
        ]
    }
] as Array<RouteRecordRaw>;
