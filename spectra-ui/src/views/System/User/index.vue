<script setup lang="ts">
import { ref } from "vue";
import UserApi from "@/api/UserApi.ts";
import { ElMessage, ElMessageBox } from "element-plus";
import UseTable from "@/hooks/UseTable.ts";
import UserEdit from "./components/Edit/index.vue";
import _ from "lodash";

// 编辑组件
const dialog_edit = ref({
    form: {} as User | undefined,
    open: false
});

// 查询条件
const condition = ref<UserPageParams>({
    page_num: 1,
    page_size: 10
});

// table分页请求
const { handleCurrentChange, handleSizeChange, handlerConditionQuery, pagination, table_data } = UseTable<User>(
    UserApi.page,
    condition.value
);

// 用户新增或编辑dialog配置
function handleUserEditDialog(row?: User | undefined) {
    let form;
    if (row != undefined) {
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
        form = datum;
    }
    dialog_edit.value = {
        form: form,
        open: true
    };
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

// 用户重置密码
function handleTableItemResetPassword(row: User) {
    console.log(`重置密码:${JSON.stringify(row)}`);
    ElMessageBox.confirm(`是否要重置[${row.name}]的密码`, "提示", { type: "warning" }).then(() => {
        UserApi.passwordResetById(row.id).then(() => {
            ElMessage.success({
                message: "重置成功",
                onClose() {
                    handlerConditionQuery();
                }
            });
        });
    });
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

// 处理dialog框关闭,如果有其他的dialog也在这里处理关闭
function handleDialogClose() {
    if (dialog_edit.value.open) {
        dialog_edit.value = {
            open: false,
            form: {} as User
        };
    }
    // 最后重新获取下列表数据
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
            <el-form-item label="状态" prop="status">
                <el-select v-model="condition.status" placeholder="请输入状态" clearable style="width: 200px">
                    <el-option label="激活" :value="true" />
                    <el-option label="冻结" :value="false" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handlerConditionQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button @click="handleUserEditDialog()">新增用户</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <!-- 列表 -->
        <el-table :data="table_data" height="92%" stripe @sort-change="handleTableSortChange">
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
                    <el-button link type="primary" size="small" @click="handleTableItemResetPassword(scope.row)">
                        重置密码
                    </el-button>
                    <el-button link type="primary" size="small" @click="handleUserEditDialog(scope.row)">
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
            layout="total, sizes, prev, pager, next"
            :default-page-size="pagination.default_page_size"
            :page-sizes="pagination.page_sizes"
            :total="pagination.total"
            style="padding: 0 10px; margin-left: auto"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange" />
    </el-row>
    <!-- 用户组件区 -->
    <user-edit :open="dialog_edit.open" :form="dialog_edit.form" @close="handleDialogClose" />
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
