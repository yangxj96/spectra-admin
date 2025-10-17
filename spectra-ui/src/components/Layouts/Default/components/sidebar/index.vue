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
        <el-menu-item index="/">
            <icons name="icon-home" class-name="icon-sidebar" />
            <template #title>首页</template>
        </el-menu-item>

        <!-- 动态菜单：根据是否有 children 决定渲染方式 -->
        <template v-for="item in menus" :key="item.path">
            <!-- 情况1：有子菜单，渲染为 el-sub-menu -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
                <template #title>
                    <icons :name="item.icon" class-name="icon-sidebar" />
                    <span>{{ item.name }}</span>
                </template>
                <el-menu-item
                    v-for="o in item.children"
                    :key="o.path"
                    :index="item.path + '/' + o.path"
                    :route="{ path: item.path + '/' + o.path }"
                >
                    <icons :name="o.icon" class-name="icon-sidebar" />
                    {{ o.name }}
                </el-menu-item>
            </el-sub-menu>

            <!-- 情况2：无子菜单，直接渲染为 el-menu-item -->
            <el-menu-item v-else :index="item.path" :route="{ path: item.path }">
                <template #title>
                    <icons :name="item.icon" class-name="icon-sidebar" />
                    <span>{{ item.name }}</span>
                </template>
            </el-menu-item>
        </template>
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
