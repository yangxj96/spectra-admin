<template>
    <el-select
        v-model="selectedIcon"
        popper-class="icon-select"
        :teleported="false"
        filterable
        placeholder="请选择图标"
        clearable>
        <el-option v-for="(icon, index) in icons" :key="index" :label="icon" :value="icon">
            <el-tooltip :content="icon">
                <icons :name="icon" class-name="icon-sidebar" />
            </el-tooltip>
        </el-option>
        <template #prefix>
            <icons v-if="selectedIcon" :name="selectedIcon" class-name="icon-sidebar" />
        </template>
    </el-select>
</template>

<script setup>
import { ref } from "vue";
import Icons from "@/components/common/Icons.vue";

const icons = ref([
    "icon-fullScreen",
    "icon-setting-role",
    "icon-fold-right",
    "icon-fold-left",
    "icon-del",
    "icon-module",
    "icon-qq",
    "icon-github",
    "icon-im-lessen",
    "icon-IM",
    "icon-log",
    "icon-role-manager",
    "icon-reset-password",
    "icon-caret-right",
    "icon-caret-left",
    "icon-enable",
    "icon-search",
    "icon-edit",
    "icon-code",
    "icon-login",
    "icon-eye",
    "icon-disable",
    "icon-xml",
    "icon-add",
    "icon-file-config",
    "icon-menu",
    "icon-setting",
    "icon-flow"
]);

const selectedIcon = defineModel();
</script>

<style lang="scss" scoped>
.icon-sidebar {
    width: 1.4em;
    height: 1.4em;
}

:deep(.icon-select) {
    max-width: 500px;

    ul.el-select-dropdown__list {
        display: flex !important;
        flex-wrap: wrap;
        overflow: hidden;

        li.el-select-dropdown__item {
            width: 10%;
            padding: 5px 0;
            text-align: center;
        }
    }
}
</style>
