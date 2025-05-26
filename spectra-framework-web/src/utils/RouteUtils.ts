import { defineAsyncComponent } from "@vue/runtime-core";
import type { RouteRecordRaw } from "vue-router";

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
