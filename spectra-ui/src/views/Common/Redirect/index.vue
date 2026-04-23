<script setup lang="ts">
import { onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";

const route = useRoute();
const router = useRouter();

let targetPath = "/";

if (Array.isArray(route.params.path)) {
    targetPath += (route.params.path as string[]).join("/");
}
if (typeof route.params.path === "string") {
    targetPath += route.params.path;
}

onMounted(async () => {
    // 等待动态菜单加载完成
    await waitForMenuLoad().then(() => console.debug("菜单加载完成,准备跳转路由."));

    // 确保路由已添加
    if (router.hasRoute(targetPath) || isRouteExists(targetPath)) {
        await router.replace(targetPath);
    } else {
        console.warn(`[Redirect] 路由未找到: ${targetPath}`);
        // 可跳转到 404 或主页
        await router.replace("/404");
    }
});

// 等待菜单加载完成
const waitForMenuLoad = () => {
    return new Promise<void>(resolve => {
        // 如果菜单已加载，直接返回
        if (useAppStore().menus.length > 0 && !useAppStore().isFetchingMenus) {
            return resolve();
        }

        // 否则监听变化
        const unwatch = watch(
            () => [useAppStore().menus.length, useAppStore().isFetchingMenus] as const,
            ([length, isFetching]) => {
                if (length > 0 && !isFetching) {
                    unwatch(); // 停止监听
                    resolve();
                }
            },
            { immediate: true }
        );
    });
};

// 辅助函数：判断路由是否存在（更精确）
const isRouteExists = (path: string) => {
    try {
        return router.resolve(path).name !== undefined;
    } catch {
        return false;
    }
};
</script>

<template>
    <div>正在跳转...</div>
</template>
