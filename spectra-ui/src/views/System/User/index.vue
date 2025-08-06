<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from "vue";
import UserApi from "@/api/UserApi.ts";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import UseTable from "@/hooks/UseTable.ts";
import * as VerifyRules from "@/utils/VerifyRules.ts";
import _ from "lodash";
import PermissionApi from "@/api/PermissionApi.ts";
import OrganizationApi from "@/api/OrganizationApi.ts";

// 树形props配置
const treeProps = { children: "children", label: "name", value: "id" };
const formRef = useTemplateRef<FormInstance>("formRef");
const orgTree = ref<OrganizationTree[]>();

// 查询条件
const condition = ref<UserPageParams>({
    page_num: 1,
    page_size: 100
});

// table分页请求
const { handleCurrentChange, handleSizeChange, handlerConditionQuery, pagination, table_data } = UseTable<User>(
    UserApi.page,
    condition.value
);

const ready = ref(false);

// 新增或编辑
const edit = reactive({
    dialog: false,
    modify: false,
    roles: [] as Role[],
    form: {} as User,
    rules: {
        name: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        email: [
            { required: true, message: "请输入邮箱", trigger: "blur" },
            { validator: VerifyRules.email, message: "请输入正确的邮箱", trigger: "blur" }
        ],
        state: [{ required: true, message: "请选择状态", trigger: "blur" }],
        // role_ids: [{required: true, message: "请选择角色", trigger: "blur"}]
        organization_id: [{ required: true, message: "请选择所属组织", trigger: "blur" }]
    } as FormRules
});

// 挂载后执行
onMounted(() => {
    ready.value = true;
    initData();
});

// 初始化获取必备数据
function initData() {
    let request = [PermissionApi.listRole(), OrganizationApi.tree()];
    Promise.all(request).then(([role, org]) => {
        edit.roles = role.data as Role[];
        orgTree.value = org.data as OrganizationTree[];
    });
}

// 表行修改按钮被单击
function handleTableItemModify(row: User) {
    let datum = _.cloneDeep(row);
    if (datum.roles && datum.roles.length > 0) {
        if (!datum.role_ids) {
            datum.role_ids = [] as string[];
        }
        for (let role of datum.roles) {
            datum.role_ids.push(role.id);
        }
        datum.roles = [];
    }
    edit.modify = true;
    edit.form = datum;
    edit.dialog = true;
}

// 表行删除按钮被单击
function handleTableItemDelete(row: User) {
    ElMessageBox.confirm(`是否要删除[${row.name}]`, "提示", { type: "warning" }).then(() => {
        UserApi.deleteById(row.id).then(() => {
            ElMessage.success({
                message: "删除成功",
                onClose() {
                    handlerConditionQuery();
                }
            });
        });
    });
}

// 用户新增被单机
function handleUserAddDialog() {
    edit.modify = false;
    edit.form = { state: 0 } as User;
    edit.dialog = true;
}

// 新增或编辑用户
async function handleUserSave() {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        const request = edit.modify ? UserApi.modify : UserApi.created;
        await request(edit.form);
        ElMessage.success({
            message: edit.modify ? "修改用户成功" : "新增用户成功",
            onClose: () => {
                edit.dialog = false;
                handlerConditionQuery();
            }
        });
    } catch (error) {
        // 输出到控制台就好了,不需要进行提示
        console.error(error);
    }
}

// 排序字段改变
function handleTableSortChange(data: { column: User; prop: string; order: string }) {
    let order: OrderItem = {
        column: data.prop,
        asc: data.order === "ascending"
    };
    condition.value.orders = [order];
    handlerConditionQuery();
}
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true" :model="condition">
            <el-form-item label="姓名" prop="username">
                <el-input v-model="condition.username" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model="condition.email" placeholder="请输入电话" clearable />
            </el-form-item>
            <el-form-item id="form-status" label="状态" prop="status">
                <el-select
                    v-if="ready"
                    v-model="condition.status"
                    :append-to="'#form-status'"
                    placeholder="请输入状态"
                    clearable
                    style="width: 200px">
                    <el-option label="激活" :value="true" />
                    <el-option label="冻结" :value="false" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handlerConditionQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button @click="handleUserAddDialog">新增用户</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <!-- 列表 -->
        <el-table :data="table_data" height="90%" stripe @sort-change="handleTableSortChange">
            <el-table-column align="center" type="index" />
            <el-table-column align="center" :sortable="true" label="姓名" prop="name" />
            <el-table-column align="center" :sortable="true" label="邮箱" prop="email" />
            <el-table-column align="center" :sortable="true" label="状态" prop="state">
                <template #default="scope">
                    <dict-tag v-model="scope.row.state" primary_value="0" dict_code="sys_user_state" />
                </template>
            </el-table-column>
            <el-table-column align="center" label="所属组织" prop="organization_name" />
            <el-table-column align="center" label="角色" prop="roles">
                <template #default="scope">
                    <el-tag v-for="(item, idx) in scope.row.roles" :index="idx" style="margin-right: 4px">
                        {{ item.name }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column align="center" label="操作">
                <template #default="scope">
                    <el-button link type="primary" size="small" @click="handleTableItemModify(scope.row)">
                        编辑
                    </el-button>
                    <el-button link type="primary" size="small" @click="handleTableItemDelete(scope.row)">
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
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange" />
    </el-row>
    <!-- 新增或编辑 -->
    <el-dialog
        v-if="ready"
        v-model="edit.dialog"
        class="loading-box"
        :append-to="'.box-content'"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        :title="(edit.modify ? '编辑' : '新增') + '用户'"
        width="30vw">
        <template #default>
            <el-form ref="formRef" :model="edit.form" :rules="edit.rules" label-width="auto" @submit.prevent>
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="edit.form.name" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="edit.form.email" placeholder="请输入邮箱">
                        <template #suffix>
                            <el-tooltip effect="dark" content="同时也作为登录账号" placement="right">
                                <icons name="icon-hint" style="margin-left: 10px; width: 1.4em; height: 1.4em" />
                            </el-tooltip>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="状态" prop="state">
                    <dict-select v-model="edit.form.state" dict_code="sys_user_state" placeholder="请选择状态" />
                </el-form-item>
                <el-form-item label="角色" prop="role_ids">
                    <el-select v-model="edit.form.role_ids" value-key="id" multiple placeholder="请选择角色" clearable>
                        <el-option v-for="item in edit.roles" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="所属组织" prop="organization_id">
                    <el-tree-select
                        v-model="edit.form.organization_id"
                        :data="orgTree"
                        node-key="id"
                        clearable
                        check-strictly
                        default-expand-all
                        :props="treeProps" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="() => (edit.dialog = false)">取消</el-button>
            <el-button type="primary" @click="handleUserSave">确定</el-button>
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
