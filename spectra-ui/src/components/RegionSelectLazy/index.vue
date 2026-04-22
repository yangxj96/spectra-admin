<script setup lang="ts">
import { onMounted, ref } from "vue";

import { regionApi } from "@/api/system/region.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import type { CascaderNode, CascaderProps, CascaderValue } from "element-plus";

defineOptions({
    name: "RegionSelectLazy"
});

/**
 * 对外：只要 ID
 */
const model = defineModel<string>({
    required: true
});

/**
 * full_name
 */
const name = defineModel<string>("name", {
    required: true
});

/**
 * 内部：路径数组（核心）
 */
const cascaderValue = ref<string[]>([]);

/**
 * lazy load
 */
const handleLoad: CascaderProps["lazyLoad"] = async (node: CascaderNode, resolve) => {
    try {
        const res = await regionApi.load({
            level: node.level + 1,
            id: (node.data as Region | undefined)?.id
        });

        resolve(res ?? []);
    } catch (e) {
        MessageUtils.error((e as Error).message);
        resolve([]);
    }
};

/**
 * 用户选择 → 同步 model + name
 */
const handleChange = async (value: CascaderValue | null | undefined) => {
    if (!value || !Array.isArray(value)) return;

    const last = value[value.length - 1];
    if (typeof last !== "string") return;

    // ✔ 更新对外 model（关键）
    model.value = last;

    try {
        const res = await regionApi.path(last);
        name.value = res.full_name;
    } catch (e) {
        MessageUtils.error((e as Error).message);
    }
};

/**
 * 回显核心
 */
const initPath = async () => {
    if (!model.value) return;

    try {
        const res = await regionApi.path(model.value);

        // ✔ 设置路径数组（关键）
        cascaderValue.value = res.ids;

        // ✔ 设置 full_name
        name.value = res.full_name;
    } catch (e) {
        MessageUtils.error((e as Error).message);
    }
};

onMounted(initPath);

/**
 * props
 */
const cascaderProps: CascaderProps = {
    lazy: true,
    checkStrictly: true,
    value: "id",
    label: "name",
    emitPath: true,
    lazyLoad: handleLoad
};
</script>

<template>
    <el-cascader
        v-model="cascaderValue"
        :props="cascaderProps"
        clearable
        filterable
        @change="handleChange"
        style="width: 100%" />
</template>
