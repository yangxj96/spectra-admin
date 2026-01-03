<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import UserApi from "@/api/user/UserApi.ts";
import OrganizationApi from "@/api/user/OrganizationApi.ts";
import { treeDefaultProps } from "@/utils/Config.ts";
import UseTable from "@/hooks/UseTable.ts";
import UserEdit from "./components/Edit/index.vue";
import DictTag from "@/components/DictTag/index.vue";
import useDictStore from "@/plugin/store/modules/useDictStore.ts";
import icons from "@/components/Icons/index.vue";

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

const organizationTree = ref<OrganizationTree[]>([]);

const dictStore = useDictStore();

function handleInitData() {
    OrganizationApi.tree().then(res => {
        if (res.code !== 200) {
            ElMessage.error(res.msg);
            return;
        }
        organizationTree.value = res.data!;
    });
}

// 用户新增或编辑dialog配置
function handleUserEditDialog(row: User) {
    let form;
    let datum = JSON.parse(JSON.stringify(row));
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

    dialog_edit.value = {
        form: form,
        open: true
    };
}

// 表行删除按钮被单击
function handleTableItemDelete(row: User) {
    ElMessageBox.confirm(`是否要删除[${row.username}]`, "提示", { type: "warning", appendTo: ".box-content" }).then(
        () => {
            UserApi.deleteById(row.id).then(() => {
                ElMessage.success({
                    message: "删除成功",
                    onClose() {
                        handlerConditionQuery();
                    }
                });
            });
        }
    );
}

// 用户重置密码
function handleTableItemResetPassword(row: User) {
    console.log(`重置密码:${JSON.stringify(row)}`);
    ElMessageBox.confirm(`是否要重置[${row.username}]的密码`, "提示", {
        type: "warning",
        appendTo: ".box-content"
    }).then(() => {
        UserApi.passwordResetById(row.id).then(() => {
            ElMessage.success({
                message: "重置成功",
                appendTo: ".box-content",
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

// 组织机构树节点被单击
function handleOrganizationTreeNodeClick(row: OrganizationTree) {
    condition.value.organization_id = row.id;
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

// 挂载后执行
onMounted(async () => {
    // 预加载数据
    await dictStore.getDictData("sys_user_gender");
    await dictStore.getDictData("sys_language");
    await dictStore.getDictData("sys_timezone");
    handleInitData();
});
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
                <el-button @click="handleUserEditDialog({} as User)">
                    <icons name="icon-user-add" style="width: 1.1em; height: 1.1em" />
                    &nbsp;新增用户
                </el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-col :span="4">
            <el-tree
                :data="organizationTree"
                :props="treeDefaultProps"
                empty-text="暂无组织机构"
                node-key="id"
                :default-expand-all="true"
                :expand-on-click-node="false"
                @node-click="handleOrganizationTreeNodeClick" />
        </el-col>
        <el-col :span="20">
            <!-- 列表 -->
            <el-table :data="table_data" height="92%" stripe @sort-change="handleTableSortChange">
                <el-table-column align="center" type="index" />
                <el-table-column align="center" width="150" show-overflow-tooltip label="显示名称" prop="username" />
                <el-table-column align="center" width="150" show-overflow-tooltip label="真实姓名" prop="real_name" />
                <el-table-column align="center" width="250" show-overflow-tooltip label="邮箱" prop="email" />
                <el-table-column align="center" width="080" show-overflow-tooltip label="性别" prop="gender">
                    <template v-slot:default="scope">
                        {{ dictStore.getDictItemSync("sys_user_gender", scope.row.gender)?.label }}
                    </template>
                </el-table-column>
                <el-table-column align="center" width="130" show-overflow-tooltip label="生日" prop="birthday" />
                <el-table-column align="center" width="120" show-overflow-tooltip label="手机号码" prop="phone" />
                <el-table-column align="center" width="100" show-overflow-tooltip label="国家" prop="country" />
                <el-table-column align="center" width="100" show-overflow-tooltip label="城市" prop="city" />
                <el-table-column align="center" width="150" show-overflow-tooltip label="语言" prop="language">
                    <template v-slot:default="scope">
                        {{ dictStore.getDictItemSync("sys_language", scope.row.language)?.label }}
                    </template>
                </el-table-column>
                <el-table-column align="center" width="200" show-overflow-tooltip label="时区" prop="timezone">
                    <template v-slot:default="scope">
                        {{ dictStore.getDictItemSync("sys_timezone", scope.row.timezone)?.label }}
                    </template>
                </el-table-column>
                <el-table-column align="center" width="150" show-overflow-tooltip label="状态" prop="state">
                    <template #default="scope">
                        <dict-tag v-model="scope.row.status" primary_value="0" dict_code="sys_user_state" />
                    </template>
                </el-table-column>
                <el-table-column
                    align="center"
                    width="150"
                    show-overflow-tooltip
                    label="所属组织"
                    prop="organization_name" />
                <el-table-column align="center" width="150" show-overflow-tooltip label="角色" prop="roles">
                    <template #default="scope">
                        <el-tag v-for="(item, idx) in scope.row.roles" :index="idx" style="margin-right: 4px">
                            {{ item.name }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" width="160" fixed="right" label="操作">
                    <template #default="scope">
                        <el-tooltip content="重置密码" placement="top">
                            <el-button link type="primary" @click="handleTableItemResetPassword(scope.row)">
                                <icons name="icon-reset-passwords" style="width: 1.4em; height: 1.4em"></icons>
                            </el-button>
                        </el-tooltip>
                        <el-tooltip content="编辑用户" placement="top">
                            <el-button link type="primary" @click="handleUserEditDialog(scope.row)">
                                <icons name="icon-user-edit" style="width: 1.4em; height: 1.4em"></icons>
                            </el-button>
                        </el-tooltip>
                        <el-tooltip content="删除用户" placement="top">
                            <el-button link type="primary" @click="handleTableItemDelete(scope.row)">
                                <icons name="icon-user-del" style="width: 1.4em; height: 1.4em"></icons>
                            </el-button>
                        </el-tooltip>
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
        </el-col>
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
