<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true" :model="condition">
            <el-form-item label="路由URI" prop="uri">
                <el-input v-model="condition.uri" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="路由ID" prop="route_id">
                <el-input v-model="condition.route_id" placeholder="请输入电话" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handlerConditionQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button>注册路由</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="90%" stripe>
            <el-table-column align="center" type="index" />
            <el-table-column align="center" prop="uri" label="URI" />
            <el-table-column align="center" prop="route_id" label="路由ID" />
            <el-table-column align="center" prop="predicates" label="谓语配置" />
            <el-table-column align="center" prop="filters" label="过滤配置" />
            <el-table-column align="center" prop="metadata" label="元数据" />
            <el-table-column align="center" prop="order" label="排序" />
            <el-table-column align="center" label="操作">
                <template v-slot:default="scope">
                    <el-button link type="primary" size="small">编辑</el-button>
                    <el-button link type="primary" size="small">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
            background
            hide-on-single-page
            layout="total, sizes, prev, pager, next"
            :default-page-size="pagination.default_page_size"
            :page-sizes="pagination.page_sizes"
            :total="pagination.total"
            style="padding: 10px; float: right"
            @sizeChange="handleSizeChange"
            @currentChange="handleCurrentChange" />
    </el-row>

    <!-- 新增或编辑路由 -->
    <teleport defer to=".el-main">
        <el-dialog
            v-model="operation.dialog"
            append-to-body
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :show-close="false"
            :destroy-on-close="true"
            :title="(operation.modify ? '编辑' : '新增') + '路由信息'"
            width="30vw">
            <template #default>
                <el-form
                    ref="form"
                    v-loading="operation.loading"
                    :model="operation.form"
                    :rules="operation.rules"
                    label-width="auto"
                    @submit.prevent>
                    <el-form-item label="路由ID" prop="route_id">
                        <el-input v-model="operation.form.route_id" placeholder="请输入路由ID" />
                    </el-form-item>
                    <el-form-item label="路由URI" prop="uri">
                        <el-input v-model="operation.form.uri" placeholder="请输入路由URI" />
                    </el-form-item>
                    <el-form-item label="排序" prop="order">
                        <el-input-number
                            v-model="operation.form.order"
                            :min="0"
                            :controls="false"
                            placeholder="请输入排序"
                            style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="过滤器" prop="filters">
                        <JsonEdit v-model="operation.form.filters" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="谓语处理" prop="predicates">
                        <JsonEdit v-model="operation.form.predicates" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="元数据" prop="metadata">
                        <JsonEdit v-model="operation.form.metadata" style="width: 100%" />
                    </el-form-item>
                </el-form>
            </template>
            <template #footer>
                <el-button :disabled="operation.loading" @click="handleDialogClosed">取消</el-button>
                <el-button :disabled="operation.loading" type="primary" @click="handleRouteSave">确定</el-button>
            </template>
        </el-dialog>
    </teleport>
</template>

<script setup lang="ts">
import { reactive, ref, useTemplateRef } from "vue";
import { type FormInstance, type FormRules } from "element-plus";
import PlatformApi from "@/api/PlatformApi.ts";
import UseTable from "@/hooks/UseTable.ts";
import JsonEdit from "@/components/JsonEditor/index.vue";

const form = useTemplateRef<FormInstance>("form");
// 查询条件
const condition = ref<RoutePageParams>({
    page_num: 1,
    page_size: 100
});

// table分页请求
const { handleCurrentChange, handleSizeChange, handlerConditionQuery, pagination, table_data } = UseTable<Route>(
    PlatformApi.page,
    condition.value
);

// 新增或编辑
const operation = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as Route,
    rules: {
        route_id: [{ required: true, message: "请输入路由ID", trigger: "blur" }],
        uri: [{ required: true, message: "请输入路由URI", trigger: "blur" }],
        order: [{ required: true, message: "请输入排序", trigger: "blur" }],
        filters: [],
        predicates: [],
        metadata: []
    } as FormRules
});

// 关闭dialog框
function handleDialogClosed() {
    operation.dialog = false;
    operation.modify = false;
    operation.loading = false;
    operation.form = {} as Route;
}

// 保存路由信息
function handleRouteSave() {
    /* 暂时为空 */
}
</script>

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
