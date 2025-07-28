<script setup lang="ts">
import Icons from "@/components/Icons/index.vue";

const model = defineModel<string>();

// 包装成一个非空 Ref<string>
const safeModel = computed(() => model.value ?? "");

const icons = shallowRef([
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
            <icons v-if="safeModel !== ''" :name="safeModel" class-name="icon-sidebar" />
        </template>
    </el-select>
</template>

<style scoped lang="scss">
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
