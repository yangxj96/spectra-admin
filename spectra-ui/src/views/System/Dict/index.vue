<script setup lang="ts">
import { defineAsyncComponent, markRaw, reactive, ref, watch } from "vue";

import { dictApi } from "@/api/system/dict.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import DictTag from "@/components/DictTag/index.vue";
import { MessageUtils } from "@/utils/message-utils.ts";

// 树形props配置
const treeProps = { children: "children", label: "name", value: "id" };

// 动态组件,字典组编辑和字典数据编辑
const dynamic = reactive({
    component: undefined as unknown,
    row: {} as unknown,
    group: {} as DictGroup | unknown,
    show: false
});

// 字典组表单数据
const dictGroupTableData = ref<DictTypeTree[]>([]);

// 字典数据表单数据
const dictDataTableData = ref<DictItem[]>([]);

// 当前选中的字典组
const currentGroup = ref<DictTypeTree>();

// 监听当前字典组的变化
watch(
    () => currentGroup.value,
    newVal => {
        if (newVal) {
            if (newVal.children && newVal.children.length > 0) {
                return;
            }
            handleGetDictData();
        } else {
            // 如果没有选中任何字典组，清空字典数据表格
            dictDataTableData.value = [];
        }
    }
);

// 初始化数据
const initData = async () => {
    dictGroupTableData.value = await dictApi.getTypesGroupTree();
};

const handleGetDictData = async () => {
    // 如果当前字典组有值，获取对应的字典数据
    dictDataTableData.value = await dictApi.getDataByTypeCode(currentGroup.value!.code);
};

const handleDialogOpen = (type: string, row: DictGroup | DictItem | unknown = {} as unknown) => {
    let Component;
    switch (type) {
        case "DictGroup": {
            Component = defineAsyncComponent(() => import("./components/DictGroupEdit/index.vue"));
            break;
        }
        case "DictData": {
            Component = defineAsyncComponent(() => import("./components/DictDataEdit/index.vue"));
            break;
        }
        default: {
            MessageUtils.error("组件加载失败,请检查");
            return;
        }
    }
    if (Component) {
        dynamic.component = markRaw(Component);
        dynamic.row = row;
        dynamic.group = currentGroup.value;
        dynamic.show = true;
    }
};

// 弹框关闭后重载数据
const handleDialogClose = () => {
    dynamic.show = false;
    initData();
    if (currentGroup.value) {
        handleGetDictData();
    }
};

initData();
</script>

<template>
    <!-- 过滤行 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item>
                <el-button v-owner="'DICT:INSERT'" @click="handleDialogOpen('DictGroup')">
                    <ComponentsIcons name="icon-edit" />
                    新增字典组
                </el-button>
                <el-button v-owner="'DICT:INSERT'" @click="handleDialogOpen('DictData')">
                    <ComponentsIcons name="icon-edit" />
                    新增字典数据
                </el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据行 -->
    <el-row class="box-body">
        <el-col :span="4">
            <el-tree
                node-key="id"
                :data="dictGroupTableData"
                highlight-current
                default-expand-all
                :props="treeProps"
                @node-click="
                    (node: DictTypeTree) => {
                        currentGroup = node;
                    }
                ">
                <template #default="{ node, data }">
                    <p class="tree-node__label">
                        {{ node.label }}
                        <ComponentsIcons v-if="data.builtin" name="icon-builtin" class-name="icon-sidebar" />
                        <el-button
                            v-if="!data.builtin"
                            v-owner="'DICT:UPDATE'"
                            class="tree-node__label-btn"
                            link
                            type="primary"
                            @click="handleDialogOpen('DictGroup', data)">
                            编辑
                        </el-button>
                    </p>
                </template>
            </el-tree>
        </el-col>
        <el-col :span="20">
            <el-row>
                <el-text size="large">编码: {{ currentGroup?.code }}</el-text>
            </el-row>
            <el-table :data="dictDataTableData" row-key="id" stripe height="75vh">
                <template #empty>
                    <p>无字典项</p>
                </template>
                <el-table-column align="center" type="index" />
                <el-table-column align="center" label="标签" prop="label" />
                <el-table-column align="center" label="值" prop="value" />
                <el-table-column align="center" label="排序" prop="sort" />
                <el-table-column align="center" label="状态" prop="state">
                    <template #default="scope">
                        <DictTag v-model="scope.row.state" primary_value="0" dict_code="sys_common_state" />
                    </template>
                </el-table-column>
                <el-table-column align="center" label="默认">
                    <template #default="scope">
                        <el-text v-if="scope.row.default_flag" type="primary">默认</el-text>
                    </template>
                </el-table-column>
                <el-table-column align="center" label="备注" prop="remark" :show-overflow-tooltip="true" />
                <el-table-column
                    v-show="!currentGroup?.builtin"
                    v-owner.or="['DICT:UPDATE', 'DICT:DELETE']"
                    align="center"
                    label="操作">
                    <template #default="scope">
                        <el-button
                            v-owner="'DICT:UPDATE'"
                            link
                            type="primary"
                            @click="handleDialogOpen('DictData', scope.row)">
                            编辑
                        </el-button>
                        <el-button v-owner="'DICT:DELETE'" link type="primary">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-col>
    </el-row>

    <!-- 动态组件,字典组或者字典数据的编辑或新增弹框 -->
    <component
        :is="dynamic.component"
        v-if="dynamic.show"
        :row="dynamic.row"
        :group="dynamic.group"
        @close="handleDialogClose" />
</template>

<style scoped lang="scss">
.box-search {
    height: 10%;
    display: flex;
    align-items: center;
    padding-left: 1em;
    padding-right: 1em;

    .el-form-item {
        margin-bottom: 0;
    }
}

.box-body {
    padding-left: 1em;
    padding-right: 1em;
    height: 90%;
}

.icon-sidebar {
    width: 1.3em;
    height: 1.3em;
}

.tree-node__label {
    width: 100%;
}

.tree-node__label-btn {
    float: right;
    margin-right: 1em;
}
</style>
