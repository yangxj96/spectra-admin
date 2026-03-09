<script setup lang="ts">
import TaskEdit from "./components/Edit.vue";

const tableData = [] as unknown[];

for (let i = 0; i < 21; i++) {
    tableData.push({
        name: "项目使用率(品类)",
        bean: "projectUsageCategoryTask",
        method: "execute",
        args: "{id:100}",
        cron: "0 0 0 * * ?",
        status: "运行中",
        remarks: "自动统计"
    });
}
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="任务名称">
                <el-input placeholder="请输入任务名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary">查询</el-button>
                <el-button>重置</el-button>
                <el-button type="primary">新增</el-button>
                <el-button type="warning">执行日志</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="tableData" border style="width: 100%; height: 70vh">
            <el-table-column align="center" width="250" prop="name" show-overflow-tooltip label="任务名称" />
            <el-table-column align="center" width="200" prop="bean" show-overflow-tooltip label="BEAN名称" />
            <el-table-column align="center" width="200" prop="method" show-overflow-tooltip label="执行方法" />
            <el-table-column align="center" width="250" prop="args" show-overflow-tooltip label="参数" />
            <el-table-column align="center" width="120" prop="cron" show-overflow-tooltip label="CRON表达式" />
            <el-table-column align="center" width="120" label="状态">
                <template #default="scope">
                    <el-tag type="primary">{{ scope.row.status }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column align="center" label="描述" prop="remarks" />
            <el-table-column align="center" width="220" label="操作">
                <template #default>
                    <el-button link type="primary">执行</el-button>
                    <el-button link type="primary">暂停</el-button>
                    <el-button link type="primary">编辑</el-button>
                    <el-button link type="primary">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 分页 -->
        <el-pagination
            layout="total, sizes, prev, pager, next"
            :default-page-size="10"
            :page-sizes="[10, 50, 100, 150, 300]"
            :total="1000"
            style="padding: 10px; margin-left: auto" />
    </el-row>
    <!-- 编辑框 -->
    <TaskEdit />
</template>

<style scoped lang="scss">
.box-search {
    padding-left: 10px;
    padding-right: 10px;
    padding-top: 10px;
}

.box-body {
    padding-left: 10px;
    padding-right: 10px;
}
</style>
