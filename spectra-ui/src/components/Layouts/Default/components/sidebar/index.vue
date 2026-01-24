<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import { stopAllRequest } from "@/plugin/request";
import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";

defineOptions({
    name: "LayoutsSidebar"
});

const appstore = useAppStore();
const unfold = ref(true);
const menus = ref<Menu[]>([]);
const active = ref("");

// 监听
appstore.$subscribe((_, state) => {
    unfold.value = state.unfold;
    loadMenus();
});

onMounted(() => {
    unfold.value = appstore.unfold;
    loadMenus();
});

const loadMenus = async () => {
    menus.value = appstore.currentMenus;
    await nextTick();
    active.value = appstore.currentMenuActive;
};

function onMenuItemClick() {
    stopAllRequest();
}
</script>

<template>
    <el-menu
        class="box-menu"
        :router="true"
        :default-active="active"
        :collapse="!unfold"
        :collapse-transition="true"
        :unique-opened="true"
        @select="onMenuItemClick">
        <!-- 动态菜单：根据是否有 children 决定渲染方式 -->
        <template v-for="item in menus" :key="item.path">
            <!-- 情况1：有子菜单，渲染为 el-sub-menu -->
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
                <template #title>
                    <components-icons :name="item.icon" class-name="icon-sidebar" />
                    <span>{{ item.name }}</span>
                </template>
                <el-menu-item
                    v-for="o in item.children"
                    :key="o.path"
                    :index="item.path + '/' + o.path"
                    :route="{ path: item.path + '/' + o.path }">
                    <components-icons :name="o.icon" class-name="icon-sidebar" />
                    {{ o.name }}
                </el-menu-item>
            </el-sub-menu>

            <!-- 情况2：无子菜单，直接渲染为 el-menu-item -->
            <el-menu-item v-else :index="item.path" :route="{ path: item.path }">
                <components-icons :name="item.icon" class-name="icon-sidebar" />
                <span>{{ item.name }}</span>
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
