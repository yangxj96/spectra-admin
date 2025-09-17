<!--
  -  Copyright 2018-2025 yangxj96
  -
  -  Licensed under the Apache License, Version 2.0 (the "License");
  -  you may not use this file except in compliance with the License.
  -  You may obtain a copy of the License at
  -
  -      http://www.apache.org/licenses/LICENSE-2.0
  -
  -  Unless required by applicable law or agreed to in writing, software
  -  distributed under the License is distributed on an "AS IS" BASIS,
  -  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  -  See the License for the specific language governing permissions and
  -  limitations under the License.
  -->

<script setup lang="ts">
import AuthApi from "@/api/AuthApi.ts";
import GlobalUtils from "@/utils/GlobalUtils";
import { ElMessage } from "element-plus/es";
import { stopAllRequest } from "@/plugin/request";
import logo from "@/assets/images/logo.svg";
import avatar from "@/assets/images/avatar.png";
import usePropsStore from "@/plugin/store/modules/usePropsStore.ts";

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

        <el-col :span="20" style="padding-right: 40px"></el-col>

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
