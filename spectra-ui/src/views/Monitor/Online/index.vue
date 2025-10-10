<script setup lang="ts">
const tableData = [] as unknown[];

for (let i = 0; i < 25; i++) {
    tableData.push({
        account: "yangxj96@gmail.com",
        name: "平台管理员",
        organization_name: "Tom",
        ip: "255.255.255.255",
        address: "No. 189, Grove St, Los Angeles",
        browser: "Chrome 101",
        login_time: "2019-02-05 00:00:00"
    });
}
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="账号">
                <el-input placeholder="请输入账号" clearable />
            </el-form-item>
            <el-form-item label="姓名">
                <el-input placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="部门">
                <el-input placeholder="请选择部门" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary">查询</el-button>
                <el-button>重置</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="tableData" border style="width: 100%; height: 70vh">
            <el-table-column align="center" prop="account" label="登录账号" />
            <el-table-column align="center" prop="name" label="姓名" />
            <el-table-column align="center" width="200" prop="organization_name" label="部门" show-overflow-tooltip />
            <el-table-column align="center" width="130" prop="ip" label="登录IP" />
            <el-table-column align="center" width="150" prop="address" label="登录地点" show-overflow-tooltip />
            <el-table-column align="center" width="120" prop="browser" label="浏览器" />
            <el-table-column align="center" width="160" prop="login_time" label="登录时间" />
            <el-table-column align="center" label="操作">
                <template #default="scope">
                    <el-button link type="primary">冻结</el-button>
                    <el-button link type="primary">踢出</el-button>
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
