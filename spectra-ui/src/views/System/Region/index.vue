<script setup lang="ts">
import { ref } from "vue";

import { regionApi } from "@/api/system/region.ts";
import UseTable from "@/hooks/use-table.ts";

// 查询条件
const condition = ref<RegionPageParams>({
    page_num: 1,
    page_size: 15
});

// table分页请求
const { handleCurrentChange, handleSizeChange, handlerConditionQuery, pagination, table_data } = UseTable<Region>(
    regionApi.page,
    condition.value
);
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="部门名称" prop="name">
                <el-input placeholder="请输入部门名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handlerConditionQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button v-owner="'DEPT:INSERT'">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="95%" border stripe default-expand-all row-key="id">
            <el-table-column align="center" width="060" type="index" label="主键ID" />
            <el-table-column align="center" width="200" prop="name" label="名称" />
            <el-table-column align="center" width="300" prop="full_name" label="全称" :show-overflow-tooltip="true" />
            <el-table-column align="center" width="300" prop="short_name" label="简称" />
            <el-table-column align="center" width="300" prop="code" label="编码" />
            <el-table-column align="center" width="300" prop="path" label="路径" :show-overflow-tooltip="true" />
            <el-table-column align="center" width="300" prop="pid" label="父级" />
            <el-table-column align="center" width="300" prop="level" label="层级" />
            <el-table-column align="center" width="300" prop="status" label="状态" />
            <el-table-column align="center" width="300" prop="sort" label="排序" />
            <el-table-column align="center" width="180" label="操作" fixed="right">
                <template #default>
                    <el-button v-owner="'DEPT:UPDATE'" link type="primary" size="small">编辑</el-button>
                    <el-button v-owner="'DEPT:DELETE'" link type="primary" size="small">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 分页 -->
        <el-pagination
            layout="total, sizes, prev, pager, next"
            :default-page-size="pagination.default_page_size"
            :page-sizes="pagination.page_sizes"
            :total="pagination.total"
            style="padding: 0 10px; margin-left: auto"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange" />
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
    padding-left: 1vw;
    padding-right: 1vw;
    height: 90%;
}
</style>
