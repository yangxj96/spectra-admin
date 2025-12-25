<script setup lang="ts">
import { ref } from "vue";
import ConfiguredApi from "@/api/ConfiguredApi.ts";
import UseTable from "@/hooks/UseTable.ts";
import ConfiguredEdit from "@/views/System/Configured/components/Edit/index.vue";

const edit = ref({
    show: false,
    form: {} as Configured
});

// 查询条件
const condition = ref<ConfiguredPageParams>({
    page_num: 1,
    page_size: 10
});

// table分页请求
const { handleCurrentChange, handleSizeChange, handlerConditionQuery, pagination, table_data } = UseTable<Configured>(
    ConfiguredApi.page,
    condition.value
);

// 处理dialog框关闭,如果有其他的dialog也在这里处理关闭
function handleDialogClose() {
    if (edit.value.show) {
        edit.value = {
            show: false,
            form: {} as Configured
        };
    }
    // 最后重新获取下列表数据
    handlerConditionQuery();
}

const handleEditConfigured = () => {
    edit.value.show = true;
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
                <el-button type="primary" @click="handlerConditionQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button type="primary" @click="handleEditConfigured">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="96%" stripe default-expand-all row-key="id">
            <el-table-column align="center" prop="id" label="主键" />
            <el-table-column align="center" prop="key" label="配置键" />
            <el-table-column align="center" prop="value" label="配置值" />
            <el-table-column align="center" prop="remarks" label="备注" />
            <el-table-column align="center" label="操作">
                <template #default="">
                    <el-button v-owner="'MENU:UPDATE'" link type="primary" size="small">编辑</el-button>
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
    <!-- 用户组件区 -->
    <configured-edit :show="edit.show" :form="edit.form" @close="handleDialogClose" />
</template>

<style scoped lang="scss">
.box-search {
    height: 10%;
    display: flex;
    align-items: center;
    padding-left: 20px;
}

.box-body {
    height: 90%;
}
</style>
