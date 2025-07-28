<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import _ from "lodash";
import OrganizationApi from "@/api/OrganizationApi.ts";

// 树形props配置
const treeProps = { children: "children", label: "name", value: "id" };

const formRef = useTemplateRef<FormInstance>("formRef");
const table_data = ref<OrganizationTree[]>();

// 新增或编辑
const edit = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as Organization,
    rules: {
        name: [{ required: true, message: "请输入部门名称", trigger: "blur" }],
        code: [{ required: true, message: "请输入部门编码", trigger: "blur" }],
        type: [{ required: true, message: "请选择部门类型", trigger: "blur" }]
    } as FormRules
});

const ready = ref(false);

onMounted(() => {
    ready.value = true;
    handleCriteriaQuery();
});

// 初始化数据
function handleCriteriaQuery() {
    OrganizationApi.tree().then(res => {
        table_data.value = res.data;
    });
}

// 表行删除按钮被单击
function handleTableItemDelete(row: Organization) {
    ElMessageBox.confirm(`是否要删除[${row.name}]`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
    }).then(() => {
        console.log(`确定删除`);
        ElMessage.success("执行删除了");
    });
}

// 处理菜单Dialog打开
function handleDialogOpen(row: Organization) {
    edit.modify = row.id !== undefined;
    edit.form = _.cloneDeep(row);
    edit.dialog = true;
}

// 新增或编辑
async function handleOrganizationSave() {
    if (!formRef.value) return;
    await formRef.value?.validate(valid => {
        if (!valid) {
            return;
        }
        edit.loading = true;
        let request = edit.modify ? OrganizationApi.modify : OrganizationApi.created;
        request(edit.form)
            .finally(() => (edit.loading = false))
            .then(() => {
                ElMessage.success({
                    message: edit.modify ? "修改组织机构成功" : "新增组织机构成功",
                    onClose() {
                        edit.dialog = false;
                        handleCriteriaQuery();
                    }
                });
            });
    });
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
                <el-button @click="handleDialogOpen({} as Organization)">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="100%" stripe default-expand-all row-key="id">
            <el-table-column align="center" type="index" />
            <el-table-column align="center" prop="name" label="名称" />
            <el-table-column align="center" prop="code" label="代码" />
            <el-table-column align="center" prop="type" label="类型">
                <template #default="scope">
                    <dict-tag v-model="scope.row.type" dict_code="sys_organization_type" />
                </template>
            </el-table-column>
            <el-table-column align="center" prop="manager_name" label="负责人" />
            <el-table-column align="center" prop="address" label="地址" show-overflow-tooltip />
            <el-table-column align="center" prop="remark" label="说明" show-overflow-tooltip />
            <el-table-column align="center" label="操作">
                <template #default="scope">
                    <el-button link type="primary" size="small" @click="handleDialogOpen(scope.row)">编辑</el-button>
                    <el-button link type="primary" size="small" @click="handleTableItemDelete(scope.row)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </el-row>
    <!-- 新增或编辑 -->
    <el-dialog
        v-if="ready"
        v-model="edit.dialog"
        :append-to="'.box-content'"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        :title="(edit.modify ? '编辑' : '新增') + '部门'"
        width="30vw">
        <template #default>
            <el-form
                ref="formRef"
                v-loading="edit.loading"
                :rules="edit.rules"
                :model="edit.form"
                label-width="auto"
                style="padding: 20px"
                @submit.prevent>
                <el-form-item v-if="edit.modify" label="数据ID" prop="id">
                    <span>{{ edit.form.id }}</span>
                </el-form-item>
                <el-form-item label="父级" prop="pid">
                    <el-tree-select
                        v-model="edit.form.pid"
                        default-expand-all
                        check-strictly
                        :data="table_data"
                        node-key="id"
                        :props="treeProps" />
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="edit.form.name" clearable placeholder="请输入部门名称" />
                </el-form-item>
                <el-form-item label="编码" prop="code">
                    <el-input v-model="edit.form.code" clearable placeholder="请输入部门编码" />
                </el-form-item>
                <el-form-item label="类型" prop="type">
                    <dict-select
                        v-model="edit.form.type"
                        dict_code="sys_organization_type"
                        placeholder="请选择部门类型" />
                </el-form-item>
                <el-form-item label="地址" prop="address">
                    <el-input v-model="edit.form.address" clearable placeholder="请输入部门地址" />
                </el-form-item>
                <el-form-item label="负责人" prop="manager_id">
                    <el-input v-model="edit.form.manager_id" disabled clearable placeholder="请选择部门负责人" />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input
                        v-model="edit.form.remark"
                        type="textarea"
                        :rows="5"
                        clearable
                        placeholder="请输入相关备注" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button :disabled="edit.loading" @click="() => (edit.dialog = false)">取消</el-button>
            <el-button :disabled="edit.loading" type="primary" @click="handleOrganizationSave()">确定</el-button>
        </template>
    </el-dialog>
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
