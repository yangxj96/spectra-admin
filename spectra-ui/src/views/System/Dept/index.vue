<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import OrganizationEdit from "./components/Edit/index.vue";
import { organizationApi } from "@/api/user/organization.ts";
import DictTag from "@/components/DictTag/index.vue";
import { MessageUtils } from "@/utils/message-utils.ts";

const table_data = ref<OrganizationTree[]>();

// 新增或编辑
const edit = reactive({
    dialog: false,
    form: {} as Organization
});

const ready = ref(false);

onMounted(() => {
    ready.value = true;
    handleCriteriaQuery();
});

// 初始化数据
function handleCriteriaQuery() {
    organizationApi.tree().then(res => {
        table_data.value = res.data;
    });
}

// 表行删除按钮被单击
function handleTableItemDelete(row: Organization) {
    MessageUtils.box.confirm(`是否要删除[${row.name}]`, "提示").then(async () => {
        try {
            const { code, msg } = await organizationApi.deleteById(row.id);
            MessageUtils.success(code === 200 ? "删除成功" : msg);
        } finally {
            handleCriteriaQuery();
        }
    });
}

// 处理菜单Dialog打开
function handleDialogOpen(row: Organization) {
    edit.form = JSON.parse(JSON.stringify(row));
    edit.dialog = true;
}

// 关闭弹窗
function handleDialogClose() {
    if (edit.dialog) {
        edit.dialog = false;
        edit.form = {} as Organization;
    }
    handleCriteriaQuery();
}
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="部门名称" prop="name">
                <el-input placeholder="请输入部门名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleCriteriaQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button v-owner="'DEPT:INSERT'" @click="handleDialogOpen({} as Organization)">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="100%" stripe default-expand-all row-key="id">
            <el-table-column align="center" width="060" type="index" label="序号" />
            <el-table-column align="center" prop="name" label="名称" />
            <el-table-column align="center" width="300" prop="code" label="编码" />
            <el-table-column align="center" width="150" prop="type" label="类型">
                <template #default="scope">
                    <dict-tag v-model="scope.row.type" dict_code="sys_organization_type" />
                </template>
            </el-table-column>
            <el-table-column align="center" prop="remark" label="说明" show-overflow-tooltip />
            <el-table-column align="center" width="180" label="操作">
                <template #default="scope">
                    <el-button
                        v-owner="'DEPT:UPDATE'"
                        link
                        type="primary"
                        size="small"
                        @click="handleDialogOpen(scope.row)">
                        编辑
                    </el-button>
                    <el-button
                        v-owner="'DEPT:DELETE'"
                        link
                        type="primary"
                        size="small"
                        @click="handleTableItemDelete(scope.row)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </el-row>
    <!-- 新增或编辑 -->
    <organization-edit :show="edit.dialog" :form="edit.form" :tree="table_data!" @close="handleDialogClose" />
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
