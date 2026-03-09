<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

import DictSelect from "@/components/DictSelect/index.vue";

const router = useRouter();
const condition = ref({
    name: undefined,
    type: undefined
});

const table_data = [
    {
        name: "员工请假审批1",
        version: 100,
        group: "人事类",
        deploy_time: "2025-10-10 00:11:22",
        suspended: false,
        remark: "这是说明"
    }
];

const handleFlowEdit = () => {
    router.push({ path: "/system/flow-edit" });
};

const handleFormEdit = () => {
    router.push({ path: "/system/form-edit" });
};
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true" :model="condition">
            <el-form-item label="流程名称" prop="name">
                <el-input v-model="condition.name" placeholder="请输入流程名称" clearable style="width: 10vw" />
            </el-form-item>
            <el-form-item label="相关分类" prop="type">
                <DictSelect
                    v-model="condition.type"
                    placeholder="请选择相关类型"
                    dict_code="dict_workflow_type"
                    style="width: 10vw" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary">查询</el-button>
                <el-button>重置</el-button>
                <el-button type="success" @click="handleFlowEdit">新增流程</el-button>
                <el-button type="success" @click="handleFormEdit">新增表单</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <!-- 列表 -->
        <el-table :data="table_data" height="92%" stripe>
            <el-table-column align="center" type="index" />
            <el-table-column align="center" label="名称" prop="name" />
            <el-table-column align="center" label="版本" prop="version" />
            <el-table-column align="center" label="分类" prop="group" />
            <el-table-column align="center" label="部署时间" prop="deploy_time" />
            <el-table-column align="center" label="是否挂起" prop="suspended">
                <template #default="scope">
                    <el-tag :type="scope.row.suspended ? 'danger' : 'primary'">
                        {{ scope.row.suspended ? "是" : "否" }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column align="center" label="说明" prop="remark" show-overflow-tooltip />
            <el-table-column align="center" label="操作">
                <template #default>
                    <el-button link type="primary" size="small">编辑</el-button>
                    <el-button link type="primary" size="small">挂起</el-button>
                    <el-button link type="primary" size="small">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 分页 -->
        <el-pagination
            layout="total, sizes, prev, pager, next"
            :default-page-size="100"
            :page-sizes="[100, 200, 300]"
            :total="1000"
            style="padding: 0 10px; margin-left: auto" />
    </el-row>
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
