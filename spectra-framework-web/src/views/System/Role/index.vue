<template>
    <el-row style="height: 100%; padding: 10px">
        <!-- 角色 -->
        <el-col :span="16">
            <!-- 过滤条件 -->
            <el-row>
                <el-form :inline="true">
                    <el-form-item label="角色名称">
                        <el-input placeholder="请输入角色名称" clearable style="width: 170px" />
                    </el-form-item>
                    <el-form-item label="角色状态">
                        <el-select placeholder="请输入选择角色状态" clearable style="width: 170px">
                            <el-option label="启用" value="启用" />
                            <el-option label="禁用" value="禁用" />
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary">查询</el-button>
                        <el-button>新增</el-button>
                    </el-form-item>
                </el-form>
            </el-row>
            <!-- 表格 -->
            <el-table :data="table_data" border highlight-current-row height="88%" style="width: 100%">
                <el-table-column align="center" label="序号" width="60" type="index" />
                <el-table-column align="center" prop="name" width="220" label="名称" />
                <el-table-column align="center" width="220" label="范围">
                    <template #default="scope">
                        <el-tag>{{ scope.row.scope }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" width="220" label="状态">
                    <template #default="scope">
                        <el-tag>{{ scope.row.state }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" prop="details" label="备注" show-overflow-tooltip />
                <el-table-column align="center" label="编辑" width="120">
                    <template #default>
                        <el-button link type="primary" size="small">编辑</el-button>
                        <el-button link type="primary" size="small">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                style="padding: 10px; float: right"
                :total="400"
                :page-sizes="[100, 200, 300, 400]"
                hide-on-single-page
                layout="total, sizes, prev, pager, next, jumper" />
        </el-col>
        <!-- 权限 -->
        <el-col :span="4" style="padding: 10px">
            <el-text type="primary">角色权限</el-text>
            <el-divider />
            <el-tree :data="tree_data" :props="tree_props" default-expand-all empty-text="暂无权限" show-checkbox />
        </el-col>
        <!-- 菜单 -->
        <el-col :span="4" style="padding: 10px">
            <el-text type="primary">角色菜单</el-text>
            <el-divider />
            <el-tree :data="tree_data" :props="tree_props" default-expand-all empty-text="暂无菜单" show-checkbox />
        </el-col>
    </el-row>

    <!-- 编辑框 -->
    <el-dialog
        v-model="edit.dialog"
        :title="'编辑角色'"
        append-to-body
        width="30%"
        :show-close="false"
        :destroy-on-close="true"
        :close-on-click-modal="false"
        :close-on-press-escape="false">
        <template #default>
            <el-form ref="ruleFormRef" label-width="auto">
                <el-form-item label="角色名称">
                    <el-input clearable />
                </el-form-item>
                <el-form-item label="角色范围">
                    <el-input clearable />
                </el-form-item>
                <el-form-item label="自定范围">
                    <el-tree-select
                        :data="tree_data"
                        show-checkbox
                        :props="tree_props"
                        :render-after-expand="false"
                        default-expand-all
                        clearable
                        multiple
                        collapse-tags
                        collapse-tags-tooltip
                        style="width: 100%" />
                </el-form-item>
                <el-form-item label="是否启用">
                    <el-switch clearable />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input type="textarea" clearable />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button>关闭</el-button>
            <el-button>提交</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import MenuApi from "@/api/MenuApi.ts";

const tree_props = {
    label: "name"
};

const table_data = reactive([
    {
        name: "超级管理员1",
        scope: "本级",
        state: "正常",
        details: "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    },
    {
        name: "超级管理员2",
        scope: "本级及其下级",
        state: "冻结",
        details: "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    },
    {
        name: "超级管理员3",
        scope: "自定义部门",
        state: "冻结",
        details: "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    },
    {
        name: "超级管理员4",
        scope: "全局",
        state: "冻结",
        details: "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    }
]);

const tree_data = ref<Menu[]>();

const edit = reactive({
    dialog: false
});

onMounted(() => {
    MenuApi.tree().then(res => (tree_data.value = res.data));
});

function handleRoleEditDialog() {
    edit.dialog = true;
}
</script>
