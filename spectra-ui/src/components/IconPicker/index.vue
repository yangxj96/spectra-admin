<script setup lang="ts">
import { computed, shallowRef } from "vue";

import ComponentsIcons from "@/components/ComponentsIcons/index.vue";

defineOptions({
    name: "IconPicker"
});

const model = defineModel<string>();

// 包装成一个非空 Ref<string>
const safeModel = computed(() => model.value ?? "");

const ico_list = shallowRef([
    "icon-home",
    "icon-setting-role",
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
    "icon-flow",
    "icon-sun",
    "icon-moon"
]);
</script>

<template>
    <el-select
        v-model="model"
        popper-class="picker-icon-select"
        :teleported="false"
        filterable
        placeholder="请选择图标"
        clearable>
        <el-option v-for="(icon, index) in ico_list" :key="index" :label="icon" :value="icon">
            <el-tooltip :content="icon">
                <ComponentsIcons :name="icon" class-name="picker-icon-sidebar" />
            </el-tooltip>
        </el-option>
        <template #prefix>
            <ComponentsIcons v-if="safeModel !== ''" :name="safeModel" class-name="picker-icon-sidebar" />
        </template>
    </el-select>
</template>

<style scoped lang="scss">
.picker-icon-sidebar {
    width: 1.4em;
    height: 1.4em;
}

:deep(.picker-icon-select) {
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
