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
            <el-form-item label="状态" prop="status">
                <el-select v-model="condition.status" placeholder="请输入状态" clearable style="width: 200px">
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
        <el-col :span="4">
            <el-auto-resizer>
                <template #default="{ height, width }">
                    <el-tree-v2
                        highlight-current
                        :data="data_tree"
                        empty-text="暂无数据"
                        :height="height"
                        :width="width" />
                </template>
            </el-auto-resizer>
        </el-col>
        <el-col :span="20">
            <el-table :data="table_data" height="90%" stripe>
                <el-table-column align="center" type="index" />
                <el-table-column align="center" prop="username" label="姓名" />
                <el-table-column align="center" prop="account" label="账号" />
                <el-table-column align="center" prop="telephone" label="电话" />
                <el-table-column align="center" prop="dept" label="岗位/部门" />
                <el-table-column align="center" label="状态">
                    <template v-slot:default="scope">
                        <el-tag v-if="scope.row.status" type="success">激活</el-tag>
                        <el-tag v-else type="danger">冻结</el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" prop="role" label="角色" />
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

    <!-- 新增或编辑用户 -->
    <el-dialog
        v-model="user.dialog"
        append-to-body
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        :title="(user.modify ? '编辑' : '新增') + '用户'"
        width="30vw">
        <template #default>
            <el-form v-loading="user.loading" :model="user.form" :rules="user.rules" label-width="auto" @submit.prevent>
                <el-form-item label="姓名" prop="username">
                    <el-input v-model="user.form.username" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="账号" prop="account">
                    <el-input v-model="user.form.account" placeholder="请输入账号" />
                </el-form-item>
                <el-form-item label="手机号码" prop="telephone">
                    <el-input v-model="user.form.telephone" placeholder="请输入手机号码" />
                </el-form-item>
                <el-form-item label="部门" prop="dept">
                    <el-tree-select
                        v-model="user.form.dept"
                        :data="data_dept_tree"
                        placeholder="请选择部门"
                        check-strictly
                        :render-after-expand="false"
                        style="width: 100%" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="user.form.status">
                        <el-radio :value="true">激活</el-radio>
                        <el-radio :value="false">冻结</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="角色" prop="roles">
                    <el-select v-model="user.form.role" placeholder="请选择角色" clearable style="width: 100%">
                        <el-option label="系统管理员" value="sysadmin" />
                        <el-option label="管理员" value="admin" />
                    </el-select>
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button :disabled="user.loading" @click="() => (user.dialog = false)">取消</el-button>
            <el-button :disabled="user.loading" type="primary" @click="handleUserSave">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import UserApi from "@/api/UserApi.ts";
import { ElMessageBox, type FormRules } from "element-plus";
import * as VerifyRules from "@/utils/VerifyRules.ts";
import UseTable from "@/hooks/UseTable.ts";

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

const data_tree = ref([
    {
        id: "1",
        label: "树型1",
        children: [
            {
                id: "1-1",
                label: "树型1-1"
            },
            {
                id: "1-2",
                label: "树型1-2"
            },
            {
                id: "1-3",
                label: "树型1-3"
            },
            {
                id: "1-4",
                label: "树型1-4"
            }
        ]
    },
    {
        id: "2",
        label: "树型2"
    },
    {
        id: "3",
        label: "树型3"
    },
    {
        id: "4",
        label: "树型4"
    }
]);

const data_dept_tree = ref([
    {
        value: "1",
        label: "部门1",
        children: [
            {
                value: "1-1",
                label: "部门1-1"
            }
        ]
    },
    {
        value: "2",
        label: "部门2",
        children: [
            {
                value: "2-1",
                label: "部门2-1"
            },
            {
                value: "2-2",
                label: "部门2-2"
            }
        ]
    }
]);

// 新增或编辑
const user = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as User,
    rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        account: [{ required: true, message: "请输入账号", trigger: "blur" }],
        telephone: [
            { required: true, message: "请输入手机号码", trigger: "blur" },
            { validator: VerifyRules.mobile, message: "请输入正确的手机号码", trigger: "blur" }
        ],
        dept: [{ required: true, message: "请选择部门", trigger: "blur" }],
        status: [{ required: true, message: "请选择状态", trigger: "blur" }],
        roles: [{ required: true, message: "请选择角色", trigger: "blur" }]
    } as FormRules
});

// 表行修改按钮被单击
function handleTableItemModify(row: User) {
    user.modify = true;
    user.form = row;
    user.dialog = true;
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

function handleUserAddDialog() {
    user.modify = false;
    user.form = {} as User;
    user.dialog = true;
}

// 新增或编辑用户
function handleUserSave() {
    console.log(`保存:`, user.form);
    user.loading = true;
    let i = setInterval(() => {
        user.loading = false;
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
