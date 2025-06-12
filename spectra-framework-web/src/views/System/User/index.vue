<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true" :model="condition">
            <el-form-item label="姓名" prop="username">
                <el-input v-model="condition.username" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="电话" prop="telephone">
                <el-input v-model="condition.telephone" placeholder="请输入电话" clearable />
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
        <!-- 部门树 -->
        <el-col :span="4">
            <el-auto-resizer>
                <template #default="{ height, width }">
                    <el-tree
                        default-expand-all
                        highlight-current
                        empty-text="暂无数据"
                        node-key="id"
                        :data="dept_tree"
                        :height="height"
                        :width="width"
                        :props="dept_tree_props"
                        @node-click="handleDeptTreeClick" />
                </template>
            </el-auto-resizer>
        </el-col>
        <!-- 列表 -->
        <el-col :span="20">
            <el-table :data="table_data" height="90%" stripe>
                <el-table-column align="center" type="index" />
                <el-table-column align="center" label="姓名" prop="name" />
                <el-table-column align="center" label="邮箱" prop="email" />
                <el-table-column align="center" label="手机" prop="tel" />
                <el-table-column align="center" label="部门" prop="dept" />
                <el-table-column align="center" label="岗位" prop="dept" />
                <el-table-column align="center" label="状态" prop="state">
                    <template #default="scope">
                        <el-tag v-if="scope.row.state" type="success">正常</el-tag>
                        <el-tag v-else type="danger">冻结</el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" label="角色" prop="role" />
                <el-table-column align="center" label="操作">
                    <template v-slot:default="scope">
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
                @sizeChange="handleSizeChange"
                @currentChange="handleCurrentChange" />
        </el-col>
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
        :title="(edit.modify ? '编辑' : '新增') + '用户'"
        width="30vw">
        <template #default>
            <el-form v-loading="edit.loading" :model="edit.form" :rules="edit.rules" label-width="auto" @submit.prevent>
                <el-form-item label="姓名" prop="username">
                    <el-input v-model="edit.form.username" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="手机" prop="tel">
                    <el-input v-model="edit.form.tel" placeholder="请输入手机号码" />
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
                <el-form-item label="部门" prop="dept">
                    <el-tree-select
                        v-model="edit.form.dept"
                        :data="dept_tree"
                        placeholder="请选择部门"
                        check-strictly
                        default-expand-all
                        node-key="id"
                        :props="dept_tree_props"
                        :render-after-expand="false" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select v-model="edit.form.status" placeholder="请选择状态" clearable>
                        <el-option label="激活" :value="true" />
                        <el-option label="冻结" :value="false" />
                    </el-select>
                </el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-select v-model="edit.form.role" placeholder="请选择角色" clearable>
                        <el-option label="系统管理员" value="sysadmin" />
                        <el-option label="管理员" value="admin" />
                    </el-select>
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button :disabled="edit.loading" @click="() => (edit.dialog = false)">取消</el-button>
            <el-button :disabled="edit.loading" type="primary" @click="handleUserSave">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import UserApi from "@/api/UserApi.ts";
import { ElMessageBox, type FormRules } from "element-plus";
import UseTable from "@/hooks/UseTable.ts";
import * as VerifyRules from "@/utils/VerifyRules.ts";

const dept_tree_props = {
    label: "name"
};

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

const dept_tree = ref<Dept[]>([
    {
        id: "1111",
        name: "部门1",
        children: [
            {
                id: "1111-2222",
                name: "部门1-1"
            }
        ]
    },
    {
        id: "12311111",
        name: "部门2",
        children: [
            {
                id: "2222-11111",
                name: "部门2-1"
            },
            {
                id: "2222-2222",
                name: "部门2-2"
            }
        ]
    }
]);

// 新增或编辑
const edit = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as User,
    rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        tel: [
            { required: true, message: "请输入手机号码", trigger: "blur" },
            { validator: VerifyRules.mobile, message: "请输入正确的手机号码", trigger: "blur" }
        ],
        email: [
            { required: true, message: "请输入邮箱", trigger: "blur" },
            { validator: VerifyRules.email, message: "请输入正确的邮箱", trigger: "blur" }
        ],
        dept: [{ required: true, message: "请选择部门", trigger: "blur" }],
        status: [{ required: true, message: "请选择状态", trigger: "blur" }],
        role: [{ required: true, message: "请选择角色", trigger: "blur" }]
    } as FormRules
});

onMounted(() => {
    ready.value = true;
});

// 部门树被单机
function handleDeptTreeClick(node: unknown) {
    console.log(`节点被单机`, node);
}

// 表行修改按钮被单击
function handleTableItemModify(row: User) {
    edit.modify = true;
    edit.form = row;
    edit.dialog = true;
}

// 表行删除按钮被单击
function handleTableItemDelete(row: User) {
    ElMessageBox.confirm(`是否要删除[${row.username}]`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "error"
    }).then(() => {
        console.log(`确定删除`);
    });
}

// 用户新增被单机
function handleUserAddDialog() {
    edit.modify = false;
    edit.form = {} as User;
    edit.dialog = true;
}

// 新增或编辑用户
function handleUserSave() {
    console.log(`保存:`, edit.form);
    edit.loading = true;
    let i = setInterval(() => {
        edit.loading = false;
        clearInterval(i);
    }, 3000);
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
