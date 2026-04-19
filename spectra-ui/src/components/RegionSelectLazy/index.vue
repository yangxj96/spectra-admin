<script setup lang="ts">
import { regionApi } from "@/api/system/region.ts";
import { treeDefaultProps } from "@/utils/default-config.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import type { LoadFunction } from "element-plus";

defineOptions({
    name: "RegionSelectLazy"
});

/**
 * 行政区划ID
 */
const model = defineModel<string>({
    required: true
});

/**
 * full_name（回显核心）
 * 格式：云南省/保山市/隆阳区
 */
const name = defineModel<string>("name", {
    required: true
});

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
        resolve([]);
    }
};

// 格式化选择的节点
const getPathLabel = (node: TreeSelectNode, data: DataParam) => {
    // 节点已加载（正常选中）
    if (node?.pathLabels?.length) {
        return node.pathLabels.join(" / ");
    }
    // 默认值 / 懒加载未命中
    if (name.value) {
        return name.value.replaceAll("/", " / ");
    }
    // 兜底
    return data?.name ?? "";
};

/**
 * 选中时同步 full_name（关键，否则无法联动）
 */
const handleNodeClick = (data: Region) => {
    name.value = data.full_name ?? data.name ?? "";
};
</script>

<template>
    <el-tree-select
        v-model="model"
        node-key="id"
        lazy
        :load="handleLoadRegion"
        v-bind="{ 'append-to': '.box-content', ...$attrs }"
        check-strictly
        clearable
        @node-click="handleNodeClick"
        :props="treeDefaultProps">
        <template #label="{ node, data }">
            {{ getPathLabel(node, data) }}
        </template>
    </el-tree-select>
</template>

<style scoped lang="scss"></style>
