<script setup lang="ts">
import _ from "lodash";
import { onMounted, ref } from "vue";

import { configuredApi } from "@/api/system/configured.ts";
import DictTag from "@/components/DictTag/index.vue";
import UseTable from "@/hooks/use-table.ts";
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
    configuredApi.page,
    condition.value
);

onMounted(() => {
    configuredApi.json().then(res => console.log(res));
});

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

const handleEditConfigured = (row: Configured) => {
    edit.value.show = true;
    edit.value.form = _.cloneDeep(row);
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
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="95%" stripe default-expand-all row-key="id">
            <el-table-column align="center" type="index" label="序号" width="100" />
            <el-table-column align="center" prop="id" label="主键" />
            <el-table-column align="center" prop="key" label="配置键" />
            <el-table-column align="center" prop="value" label="配置值">
                <template v-slot:default="scope">
                    <!-- 布尔类型 -->
                    <el-tag v-if="scope.row.type === 'BOOL'" :type="scope.row.value === 'true' ? 'success' : 'danger'">
                        {{ scope.row.value === "true" ? "启用" : "禁用" }}
                    </el-tag>
                    <!-- 下拉选择的类型 -->
                    <DictTag
                        v-else-if="scope.row.type === 'SELECT'"
                        v-model="scope.row.value"
                        :dict_code="scope.row.dict_code" />
                    <!-- TEXT保底 -->
                    <el-text v-else>
                        {{ scope.row.value }}
                    </el-text>
                </template>
            </el-table-column>
            <el-table-column align="center" prop="remarks" label="备注" />
            <el-table-column align="center" label="操作">
                <template #default="scope">
                    <el-button
                        v-owner="'ROLE:ROLE_DEV_OPS'"
                        link
                        type="primary"
                        size="small"
                        @click="handleEditConfigured(scope.row)">
                        编辑
                    </el-button>
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
    <ConfiguredEdit :show="edit.show" :form="edit.form" @close="handleDialogClose" />
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
