<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute } from "vue-router";

import { authApi } from "@/api/auth/auth.ts";
import avatar from "@/assets/images/avatar.png";
import logo from "@/assets/images/logo.svg";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import ChangePassword from "@/components/Props/ChangePassword/index.vue";
import PersonalDetails from "@/components/Props/PersonalDetails/index.vue";
import { cancelAllRequests } from "@/plugin/request/http.ts";
import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";
import { usePropsStore } from "@/plugin/store/modules/use-props-store.ts";
import { GlobalUtils } from "@/utils/global-utils.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

defineOptions({
    name: "LayoutsNavbar"
});

// 获取路由对象（useRoute 是响应式的）
const route = useRoute();
const appStore = useAppStore();
const prefixes = ref<Menu[]>(appStore.menus);
// 定义菜单前缀映射
const menuPrefixes = ref(["/"]);
const active = ref("/");

for (const menu of appStore.menus) {
    menuPrefixes.value.push(menu.path);
}

// 监听路由变化
watch(
    () => route.path,
    path => {
        resolveSideMenus(path);
        resolveTopActive(path);
    },
    { immediate: true, flush: "pre" }
);

// 解析顶部菜单
function resolveTopActive(path: string) {
    const prefixes = ["/", ...appStore.menus.map(m => m.path)].filter(Boolean);

    // 按长度降序（最长优先）
    const sortedPrefixes = prefixes.sort((a, b) => b.length - a.length);

    for (const prefix of sortedPrefixes) {
        // 首页
        if (prefix === "/" && path === "/") {
            active.value = "/";
            return;
        }

        // 其他页
        if (prefix !== "/" && (path === prefix || path.startsWith(prefix + "/"))) {
            active.value = prefix;
            return;
        }
    }

    active.value = "/";
}

// 解析侧边栏
function resolveSideMenus(path: string) {
    // 首页单独处理
    if (path === "/") {
        appStore.currentMenus = [];
        return;
    }

    let matchedMenu: Menu | null = null;
    let maxMatchLength = -1;

    for (const menu of appStore.menus) {
        if (!menu.path || !Array.isArray(menu.children) || menu.children.length === 0) {
            continue;
        }

        const isMatch = path === menu.path || path.startsWith(menu.path + "/");

        if (!isMatch) continue;

        if (menu.path.length > maxMatchLength) {
            matchedMenu = menu;
            maxMatchLength = menu.path.length;
        }
    }

    if (matchedMenu) {
        appStore.currentMenus = matchedMenu.children ?? [];
        appStore.currentMenusPrefix = matchedMenu.path;
    } else {
        appStore.currentMenus = [];
    }
}

function handleUserLogout() {
    cancelAllRequests();
    authApi.logout();
    MessageUtils.success("退出成功", () => {
        GlobalUtils.exit();
    });
}

function handleModifyPasswordPopup() {
    usePropsStore().change_password = true;
}

function handlePersonalPopup() {
    usePropsStore().personal_details = true;
}
</script>

<template>
    <el-row style="height: 60px">
        <el-col :span="3">
            <el-image :src="logo" style="height: 55px; width: 90%" />
        </el-col>

        <el-col :span="20" style="padding-right: 40px">
            <el-menu :default-active="active" :router="true" mode="horizontal">
                <el-menu-item v-for="o in prefixes" :key="o.path" :index="o.path" :route="{ path: o.path }">
                    <ComponentsIcons :name="o.icon" class-name="icon-sidebar" />
                    {{ o.name }}
                </el-menu-item>
            </el-menu>
        </el-col>

        <el-col :span="1">
            <el-dropdown>
                <img
                    :src="avatar"
                    alt="default avatar"
                    style="object-fit: cover"
                    class="el-avatar el-avatar--circle el-tooltip__trigger" />
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item @click="handlePersonalPopup">
                            <ComponentsIcons name="icon-user" class-name="icon-navbar" />
                            <span>个人信息</span>
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleModifyPasswordPopup">
                            <ComponentsIcons name="icon-change-password" class-name="icon-navbar" />
                            <span>修改密码</span>
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleUserLogout">
                            <ComponentsIcons name="icon-logout" class-name="icon-navbar" />
                            <span>退出登录</span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </el-col>
    </el-row>
    <PersonalDetails />
    <ChangePassword />
</template>

<style scoped lang="scss">
.goto-home {
    cursor: pointer;
}

.el-menu.el-menu--horizontal {
    border: 0;
}

:deep(.el-dropdown) {
    width: 100%;
    top: 20%;
    text-align: center;
}

.icon-navbar {
    width: 1.3em;
    height: 1.3em;
    padding-right: 0.5em;
}

.flex-grow {
    flex-grow: 1;
}

:deep(.el-menu) {
    height: 100%;
}
</style>
