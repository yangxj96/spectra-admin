<template>
    <el-row style="height: 100%; padding: 10px">
        <!-- 角色 -->
        <el-col :span="16">
            <!-- 过滤条件 -->
            <el-row>
                <el-form :inline="true" :model="condition">
                    <el-form-item label="角色名称">
                        <el-input
                            v-model="condition.name"
                            placeholder="请输入角色名称"
                            clearable
                            style="width: 170px" />
                    </el-form-item>
                    <el-form-item label="角色状态">
                        <el-select
                            v-model="condition.state"
                            placeholder="请输入选择角色状态"
                            clearable
                            style="width: 170px">
                            <el-option label="启用" :value="true" />
                            <el-option label="禁用" :value="false" />
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handlerConditionQuery">查询</el-button>
                        <el-button @click="handleRoleAddDialog">新增</el-button>
                    </el-form-item>
                </el-form>
            </el-row>
            <!-- 表格 -->
            <el-table
                :data="table_data"
                border
                highlight-current-row
                height="88%"
                style="width: 100%"
                @row-dblclick="handleRoleTableRowClick">
                <el-table-column align="center" label="序号" width="60" type="index" />
                <el-table-column align="center" prop="name" width="220" label="名称" />
                <el-table-column align="center" width="220" label="范围">
                    <template #default="scope">
                        <el-tag v-if="scope.row.scope === 0" type="primary">本级及其下级</el-tag>
                        <el-tag v-else-if="scope.row.scope === 1" type="primary">本级</el-tag>
                        <el-tag v-else-if="scope.row.scope === 2" type="primary">全局</el-tag>
                        <el-tag v-else type="danger">未匹配</el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" width="220" label="状态">
                    <template #default="scope">
                        <el-tag :type="scope.row.state ? 'primary' : 'danger'">
                            {{ scope.row.state ? "启用" : "禁用" }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" prop="remark" label="备注" show-overflow-tooltip />
                <el-table-column align="center" label="编辑" width="120">
                    <template #default="scope">
                        <el-button link type="primary" size="small" @click="handleRoleEditDialog(scope.row)">
                            编辑
                        </el-button>
                        <el-button link type="primary" size="small" @click="handleRoleDelete(scope.row)">
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- 分页 -->
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
        </el-col>
        <!-- 权限 -->
        <el-col :span="4" style="padding: 10px">
            <el-text type="primary">角色权限</el-text>
            <el-divider />
            <el-tree
                ref="powerRef"
                :data="tree_data"
                :props="tree_props"
                default-expand-all
                empty-text="暂无权限"
                show-checkbox />
        </el-col>
        <!-- 菜单 -->
        <el-col :span="4" style="padding: 10px">
            <el-text type="primary">角色菜单</el-text>
            <el-divider />
            <el-tree
                ref="menuRef"
                :data="tree_data"
                :props="tree_props"
                default-expand-all
                empty-text="暂无菜单"
                show-checkbox />
        </el-col>
    </el-row>
    <!-- 编辑框 -->
    <el-dialog
        v-if="ready"
        v-model="edit.dialog"
        :title="`${edit.modify ? '编辑' : '新增'}角色`"
        :append-to="'.box-content'"
        width="30%"
        :show-close="false"
        :destroy-on-close="true"
        :close-on-click-modal="false"
        :close-on-press-escape="false">
        <template #default>
            <el-form ref="formRef" v-loading="edit.loading" :model="edit.form" :rules="edit.rules" label-width="auto">
                <el-form-item label="角色名称" prop="name">
                    <el-input v-model="edit.form.name" clearable />
                </el-form-item>
                <el-form-item label="角色范围" prop="scope">
                    <el-select v-model="edit.form.scope" clearable>
                        <el-option :value="0" label="本级及其下级" />
                        <el-option :value="1" label="本级" />
                        <el-option :value="2" label="全局" />
                    </el-select>
                </el-form-item>
                <el-form-item label="是否启用" prop="state">
                    <el-select v-model="edit.form.state" clearable>
                        <el-option :value="true" label="是" />
                        <el-option :value="false" label="否" />
                    </el-select>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="edit.form.remark" type="textarea" clearable />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button :disabled="edit.loading" @click="() => (edit.dialog = false)">关闭</el-button>
            <el-button :disabled="edit.loading" @click="handleRoleSave">提交</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from "vue";
import MenuApi from "@/api/MenuApi.ts";
import { ElMessage, ElMessageBox, ElTree, type FormInstance, type FormRules } from "element-plus";
import PermissionApi from "@/api/PermissionApi.ts";
import UseTable from "@/hooks/UseTable.ts";

const formRef = useTemplateRef<FormInstance>("formRef");
const powerRef = useTemplateRef<InstanceType<typeof ElTree>>("powerRef");
const menuRef = useTemplateRef<InstanceType<typeof ElTree>>("menuRef");

const ready = ref(false);

// 查询条件
const condition = ref<RolePageParams>({
    page_num: 1,
    page_size: 100
});

const tree_props = {
    label: "name"
};

const tree_data = ref<Menu[]>();

const edit = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as Role,
    rules: {} as FormRules
});

const { handlerConditionQuery, handleCurrentChange, handleSizeChange, pagination, table_data } = UseTable<Role>(
    PermissionApi.pageRole,
    condition.value
);

onMounted(() => {
    ready.value = true;
    handleInitData();
});

// 初始化数据
function handleInitData() {
    MenuApi.tree().then(res => (tree_data.value = res.data));
}

// 角色新增打开
function handleRoleAddDialog() {
    console.log(`角色新增`);
    edit.modify = false;
    edit.form = {} as Role;
    edit.dialog = true;
}

// 角色编辑框打开
function handleRoleEditDialog(row: Role) {
    console.log(`角色编辑`);
    edit.modify = true;
    edit.form = row;
    edit.dialog = true;
}

// 角色删除
function handleRoleDelete(row: Role) {
    console.log(`角色删除:`, row);
    ElMessageBox.confirm(`是否要删除[${row.name}]`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
    }).then(() => {
        console.log(`确定删除`);
        ElMessage.success("执行删除了");
    });
}

// 角色保存
async function handleRoleSave() {
    console.log(`角色保存:`);
    if (!formRef.value) return;
    await formRef.value?.validate((valid, _) => {
        if (valid) {
            edit.loading = true;
            let request = edit.modify ? PermissionApi.modifyRole : PermissionApi.createdRole;
            request(edit.form)
                .finally(() => (edit.loading = false))
                .then(() => {
                    ElMessage.success({
                        message: edit.modify ? "修改角色成功" : "新增角色成功",
                        onClose() {
                            edit.dialog = false;
                            handlerConditionQuery();
                        }
                    });
                });
        }
    });
}

// 角色列表行被单机
function handleRoleTableRowClick(row: Role, column: unknown, event: Event) {
    console.log(row);
    console.log(column);
    console.log(event);
}
</script>
