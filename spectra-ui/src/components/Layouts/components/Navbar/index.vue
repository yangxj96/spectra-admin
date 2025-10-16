<script setup lang="ts">
import AuthApi from "@/api/AuthApi.ts";
import GlobalUtils from "@/utils/GlobalUtils";
import { ElMessage } from "element-plus/es";
import { stopAllRequest } from "@/plugin/request";
import logo from "@/assets/images/logo.svg";
import avatar from "@/assets/images/avatar.png";
import usePropsStore from "@/plugin/store/modules/usePropsStore.ts";
import { useRoute } from "vue-router";

// 定义菜单前缀映射
const menuPrefixes = ["/", "/workbench", "/system", "/monitor", "/example"];

const active = computed(() => {
    const path = useRoute().path;
    // 从最长到最短排序，避免 /System 匹配到 /SystemManage 等情况（可选优化）
    const sortedPrefixes = [...menuPrefixes].sort((a, b) => b.length - a.length);
    for (const prefix of sortedPrefixes) {
        if (prefix === "/" && path === "/") {
            return "/";
        }
        if (prefix !== "/" && path.startsWith(prefix)) {
            return prefix;
        }
    }
    // 默认返回首页
    return "/";
});

function handleUserLogout() {
    stopAllRequest();
    AuthApi.logout().then(() => {
        ElMessage.success({
            message: "退出成功",
            onClose: () => {
                GlobalUtils.exit();
            }
        });
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
            <el-image :src="logo" />
        </el-col>

        <el-col :span="20" style="padding-right: 40px">
            <el-menu :default-active="active" :router="true" mode="horizontal">
                <el-menu-item index="/">
                    <icons name="icon-home" class-name="icon-sidebar" />
                    首页
                </el-menu-item>
                <el-menu-item index="/workbench">
                    <icons name="icon-home" class-name="icon-sidebar" />
                    工作台
                </el-menu-item>
                <el-menu-item index="/system">
                    <icons name="icon-home" class-name="icon-sidebar" />
                    系统管理
                </el-menu-item>
                <el-menu-item index="/monitor">
                    <icons name="icon-home" class-name="icon-sidebar" />
                    系统监控
                </el-menu-item>
                <el-menu-item index="/example">
                    <icons name="icon-home" class-name="icon-sidebar" />
                    组件示例
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
                            <icons name="icon-user" class-name="icon-navbar" />
                            <span>个人信息</span>
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleModifyPasswordPopup">
                            <icons name="icon-change-password" class-name="icon-navbar" />
                            <span>修改密码</span>
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleUserLogout">
                            <icons name="icon-logout" class-name="icon-navbar" />
                            <span>退出登录</span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </el-col>
    </el-row>
    <personal-details />
    <change-password />
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
