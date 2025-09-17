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
import { stopAllRequest } from "@/plugin/request";
import useAppStore from "@/plugin/store/modules/useAppStore.ts";

const route = useRoute();
const unfold = ref(true);
const menus = ref([] as Menu[]);

// 监听
useAppStore().$subscribe((_, state) => {
    unfold.value = state.unfold;
    menus.value = state.menus;
});

onMounted(() => {
    unfold.value = useAppStore().unfold;
    menus.value = useAppStore().menus;
});

function onMenuItemClick() {
    stopAllRequest();
}
</script>

<template>
    <el-menu
        class="box-menu"
        :router="true"
        :default-active="route.path"
        :collapse="!unfold"
        :collapse-transition="true"
        :unique-opened="true"
        @select="onMenuItemClick">
        <el-menu-item index="home" :route="{ path: '/' }">
            <icons name="icon-home" class-name="icon-sidebar" />
            <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu v-for="item in menus" :index="item.path">
            <template #title>
                <icons :name="item.icon" class-name="icon-sidebar" />
                <span>{{ item.name }}</span>
            </template>
            <el-menu-item v-for="o in item.children" :index="item.path + '/' + o.path">
                <icons :name="o.icon" class-name="icon-sidebar" />
                {{ o.name }}
            </el-menu-item>
        </el-sub-menu>
    </el-menu>
</template>

<style scoped lang="scss">
.box-menu {
    height: 100%;
}

.box-menu:not(.el-menu--collapse) {
    width: 100%;
}

.icon-sidebar {
    width: 1.4em;
    height: 1.4em;
    padding-right: 0.5em;
    padding-left: 0.2em;
}
</style>
