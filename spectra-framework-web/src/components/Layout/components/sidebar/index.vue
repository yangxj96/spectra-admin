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

<script lang="ts" setup>
import { stopAllRequest } from "@/plugin/request";
import { useRoute } from "vue-router";
import { onMounted, ref } from "vue";
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

<style scoped lang="scss">
.box-menu {
    height: 100%;
}

.box-menu:not(.el-menu--collapse) {
    width: 200px;
    min-height: 400px;
}

.icon-sidebar {
    width: 1.4em;
    height: 1.4em;
    padding-right: 0.5em;
    padding-left: 0.2em;
}
</style>
