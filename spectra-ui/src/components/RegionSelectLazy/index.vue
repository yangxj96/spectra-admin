<script setup lang="ts">
import { onMounted } from "vue";

import { regionApi } from "@/api/system/region.ts";
import { treeDefaultProps } from "@/utils/default-config.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import type { LoadFunction } from "element-plus";

defineOptions({
    name: "RegionSelectLazy"
});

const model = defineModel<string>({
    required: true
});

const name = defineModel<string>("name", {
    required: true
});

// 用作区域懒加载没数据的时候的回显
// const regionCache = computed(() => {
//     return {
//         id: model,
//         name: name
//     };
// });

// 懒加载行政区划
const handleLoadRegion: LoadFunction = async (node, resolve) => {
    try {
        // 构建参数
        const regions = await regionApi.load({
            level: node.level + 1,
            id: node.data?.id
        });
        resolve(regions ?? []);
    } catch (e) {
        MessageUtils.error(`获取行政区划失败:${(e as Error).message}`);
    }
};

// 格式化选择的节点
const getPathLabel = (node: TreeSelectNode, data: DataParam) => {
    // 节点已加载（正常选中）
    if (node?.pathLabels?.length) {
        return node.pathLabels.join(" / ");
    }
    // 默认值 / 懒加载未命中
    if (data?.pathLabels?.length) {
        return data.pathLabels.join(" / ");
    }
    // 兜底
    return data?.name ?? "";
};

onMounted(() => {
    console.log(model);
    console.log(name);
});
</script>

<template>
    <!--
    :cache-data="regionCache"
    -->
    <el-tree-select
        v-model="model"
        node-key="id"
        lazy
        :load="handleLoadRegion"
        v-bind="{ 'append-to': '.box-content', ...$attrs }"
        check-strictly
        clearable
        :props="treeDefaultProps">
        <template #label="{ node, data }">
            {{ getPathLabel(node, data) }}
        </template>
    </el-tree-select>
</template>

<style scoped lang="scss"></style>
