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
