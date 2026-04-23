<script setup lang="ts">
import { ElTable, type FormInstance, type FormRules } from "element-plus";
import { onMounted, reactive, ref, useTemplateRef } from "vue";

import { menuApi } from "@/api/system/menu.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import IconPicker from "@/components/IconPicker/index.vue";
import JsonEditor from "@/components/JsonEditor/index.vue";
import { MessageUtils } from "@/utils/message-utils.ts";

const menuForm = useTemplateRef<FormInstance>("ruleFormRef");
const table_data = ref<Menu[]>([]);

// 新增或编辑
const menu = reactive({
    dialog: false,
    modify: false,
    form: {} as Menu,
    rules: {
        name: [{ required: true, message: "请输入菜单名称", trigger: "blur" }],
        icon: [{ required: true, message: "请选择菜单图标", trigger: "blur" }],
        path: [{ required: true, message: "请输入菜单路径", trigger: "blur" }],
        component: [{ required: true, message: "请输入组件路径", trigger: "blur" }],
        sort: [{ required: true, message: "请输入排序", trigger: "blur" }]
    } as FormRules
});

const ready = ref(false);

onMounted(() => {
    handleCriteriaQuery();
    ready.value = true;
});

// 初始化数据
const handleCriteriaQuery = async () => {
    table_data.value = await menuApi.tree();
};

// 表行修改按钮被单击
const handleTableItemModify = (row: Menu) => {
    menu.modify = true;
    menu.form = JSON.parse(JSON.stringify(row));
    menu.dialog = true;
};

// 表行删除按钮被单击
const handleTableItemDelete = (row: Menu) => {
    MessageUtils.box.confirm(`是否要删除[${row.name}]`, "提示").then(async () => {
        console.log(`确定删除`);
        await menuApi.deleteById(row.id);
        MessageUtils.success("删除菜单成功", () => {
            handleCriteriaQuery();
        });
    });
};

// 处理菜单Dialog打开
const handleMenuAddDialog = () => {
    menu.modify = false;
    menu.form = {} as Menu;
    menu.dialog = true;
};

// 新增或编辑
const handleMenuSave = async () => {
    if (!menuForm.value) return;
    await menuForm.value?.validate(async valid => {
        if (valid) {
            if (menu.modify) {
                await menuApi.update(menu.form);
            } else {
                await menuApi.create(menu.form);
            }
            MessageUtils.success(menu.modify ? "修改菜单成功" : "新增菜单成功", () => {
                menu.dialog = false;
                handleCriteriaQuery();
            });
        }
    });
};
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="菜单名称" prop="name">
                <el-input placeholder="请输入菜单名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleCriteriaQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button v-owner="'MENU:INSERT'" @click="handleMenuAddDialog">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <ElTable
            ref="ref_table"
            v-if="table_data.length > 0"
            :data="table_data"
            height="100%"
            stripe
            row-key="id"
            class="loading-box">
            <el-table-column align="center" type="index" />
            <el-table-column align="left" header-align="center" prop="name" label="名称" />
            <el-table-column align="center" prop="icon" label="图标">
                <template #default="scope">
                    <ComponentsIcons :name="scope.row.icon" />
                </template>
            </el-table-column>
            <el-table-column align="center" prop="path" label="请求路径" />
            <el-table-column align="center" prop="component" label="组件路径" :show-overflow-tooltip="true" />
            <el-table-column align="center" prop="layout" label="布局" />
            <el-table-column align="center" prop="hide" label="隐藏">
                <template v-slot:default="scope">
                    <el-text :type="scope.row.hide ? 'success' : 'danger'">
                        {{ scope.row.hide ? "是" : "否" }}
                    </el-text>
                </template>
            </el-table-column>
            <el-table-column align="center" prop="sort" label="排序" />
            <el-table-column align="center" label="操作" v-owner.or="['MENU:UPDATE', 'MENU:DELETE']">
                <template #default="scope">
                    <el-button
                        v-owner="'MENU:UPDATE'"
                        link
                        type="primary"
                        size="small"
                        @click="handleTableItemModify(scope.row)">
                        编辑
                    </el-button>
                    <el-button
                        v-owner="'MENU:DELETE'"
                        link
                        type="primary"
                        size="small"
                        @click="handleTableItemDelete(scope.row)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </ElTable>
    </el-row>
    <!-- 新增或编辑 -->
    <el-dialog
        v-if="ready"
        v-model="menu.dialog"
        class="loading-box"
        :append-to="'.box-content'"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        width="30vw">
        <template #header>
            <ComponentsIcons name="icon-edit" />
            {{ (menu.modify ? "编辑" : "新增") + "菜单" }}
        </template>
        <template #default>
            <el-form
                ref="ruleFormRef"
                :rules="menu.rules"
                :model="menu.form"
                label-width="auto"
                style="padding: 20px"
                @submit.prevent>
                <el-form-item v-if="menu.modify" label="数据ID" prop="id">
                    <el-input v-model="menu.form.id" disabled clearable placeholder="请输入菜单名称" />
                </el-form-item>
                <el-form-item label="上级菜单" prop="pid">
                    <el-tree-select
                        v-model="menu.form.pid"
                        :data="table_data"
                        placeholder="请选择菜单父级"
                        check-strictly
                        default-expand-all
                        node-key="id"
                        append-to=".box-content"
                        :props="{ label: 'name' }"
                        :render-after-expand="false" />
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="menu.form.name" clearable placeholder="请输入菜单名称" />
                </el-form-item>
                <el-form-item label="图标" prop="icon">
                    <IconPicker v-model="menu.form.icon" />
                </el-form-item>
                <el-form-item label="是否隐藏" prop="hide">
                    <el-switch v-model="menu.form.hide" />
                </el-form-item>
                <el-form-item label="路径" prop="path">
                    <el-input v-model="menu.form.path" clearable placeholder="请输入路径" />
                </el-form-item>
                <el-form-item label="组件" prop="component">
                    <el-input v-model="menu.form.component" clearable placeholder="请输入组件路径" />
                </el-form-item>
                <el-form-item label="布局" prop="layout">
                    <el-select v-model="menu.form.layout" clearable placeholder="请输入布局" append-to=".box-content">
                        <el-option label="默认布局" value="default" />
                        <el-option label="空白布局" value="blank" />
                    </el-select>
                </el-form-item>
                <el-form-item label="元数据" prop="metadata">
                    <JsonEditor :read-only="false" :model-value="menu.form.metadata ?? {}" style="width: 100%" />
                </el-form-item>
                <el-form-item label="排序" prop="sort">
                    <el-input-number
                        v-model="menu.form.sort"
                        :min="0"
                        :max="999"
                        placeholder="请输入排序"
                        style="width: 100%" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="() => (menu.dialog = false)">取消</el-button>
            <el-button type="primary" @click="handleMenuSave()">确定</el-button>
        </template>
    </el-dialog>
</template>

<style scoped lang="scss">
.box-search {
    height: 10%;
    display: flex;
    align-items: center;
    padding-left: 20px;

    .el-form-item {
        margin-bottom: 0;
    }
}

.box-body {
    height: 90%;
}
</style>
