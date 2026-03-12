<script setup lang="ts">
const tableData = [] as unknown[];

for (let i = 0; i < 25; i++) {
    tableData.push({
        account: "yangxj96@gmail.com",
        name: "平台管理员",
        organization_name: "Tom",
        ac: [
            {
                token: "96402775-f65a-4ad2-8a04-a9fb26fbc442",
                device_type: "PC",
                create_time: "2025-10-15 00:00:00",
                ip: "255.255.255.255",
                address: "内网地址"
            }
        ]
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
            <el-table-column type="expand">
                <template #default="props">
                    <el-row style="width: 100%; padding-left: 1vw; padding-right: 1vw">
                        <el-table :data="props.row.ac" border style="width: 100%">
                            <el-table-column align="center" width="060" type="index" label="序号" />
                            <el-table-column align="center" width="300" prop="token" label="令牌" />
                            <el-table-column align="center" width="100" prop="device_type" label="设备类型" />
                            <el-table-column align="center" width="200" prop="ip" label="登录IP" />
                            <el-table-column align="center" prop="address" label="登录地点" />
                            <el-table-column align="center" width="200" prop="create_time" label="登录时间" />
                            <el-table-column align="center" label="操作">
                                <template #default>
                                    <el-button link type="primary">踢出此设备</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-row>
                </template>
            </el-table-column>
            <el-table-column align="center" prop="account" label="登录账号" />
            <el-table-column align="center" prop="name" label="姓名" />
            <el-table-column align="center" width="200" prop="organization_name" label="部门" show-overflow-tooltip />
            <el-table-column align="center" label="操作">
                <template #default>
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
