<script setup lang="ts">
import { ElMessage, ElMessageBox, ElTree } from "element-plus";
import { onMounted, reactive, ref, useTemplateRef } from "vue";
import RoleEdit from "./components/RoleEdit/index.vue";
import { treeDefaultProps } from "@/utils/Config.ts";
import UseTable from "@/hooks/UseTable.ts";
import MenuApi from "@/api/MenuApi.ts";
import RoleApi from "@/api/RoleApi.ts";
import AuthorityApi from "@/api/AuthorityApi.ts";
import _ from "lodash";

// refs
const powerRef = useTemplateRef<InstanceType<typeof ElTree>>("powerRef");
const menuRef = useTemplateRef<InstanceType<typeof ElTree>>("menuRef");

// 数据
const menu_tree = ref<Menu[]>();
const authority_tree = ref<AuthorityTree[]>();
const condition = ref<RolePageParams>({
    page_num: 1,
    page_size: 100
});
const edit = reactive({
    dialog: false,
    form: {} as Role
});
const currentRow = ref<Role>();

const { handlerConditionQuery, handleCurrentChange, handleSizeChange, pagination, table_data } = UseTable<Role>(
    RoleApi.page,
    condition.value
);

onMounted(() => {
    handleInitData();
});

// 初始化数据
function handleInitData() {
    let requests = [MenuApi.tree(), AuthorityApi.tree()];
    Promise.all(requests).then(([menuRes, authorityTreeRes]) => {
        menu_tree.value = menuRes!.data as Menu[];
        authority_tree.value = authorityTreeRes!.data as AuthorityTree[];
    });
}

// 角色编辑框Dialog
function handleRoleEditDialogOpen(row: Role) {
    edit.form = _.cloneDeep(row);
    edit.dialog = true;
}

// 角色删除
function handleRoleDelete(row: Role) {
    ElMessageBox.confirm(`是否要删除[${row.name}]`, "提示", { type: "warning" }).then(() => {
        RoleApi.delete(row.id).then(res => {
            if (res.code === 200) {
                ElMessage.success({
                    message: "删除成功"
                });
            } else {
                ElMessage.error({
                    message: res.msg
                });
            }
            handlerConditionQuery();
        });
    });
}

// 条件查询
function handleRoleConditionQuery() {
    cleanTreeCheckState();
    handlerConditionQuery();
}

// 清理右边两棵树的选中状态
function cleanTreeCheckState() {
    if (authority_tree.value)
        for (const item of authority_tree.value) {
            powerRef.value?.setChecked(item.id, false, true);
        }

    if (menu_tree.value)
        for (const item of menu_tree.value) {
            menuRef.value?.setChecked(item.id, false, true);
        }
}

// 角色列表行被单机
async function handleRoleTableRowClick(row: Role) {
    if (currentRow.value && currentRow.value.id && currentRow.value.id == row.id) return;
    try {
        currentRow.value = row;
        cleanTreeCheckState();
        // 权限部分
        RoleApi.getRoleAuthority(row.id).then(res => {
            if (res.code == 200 && res.data && res.data.length > 0) {
                let ids = res.data.map(i => i.id);
                powerRef.value?.setCheckedKeys(ids);
            }
        });

        // 菜单部分
        RoleApi.getRoleMenu(row.id).then(res => {
            if (res.code == 200 && res.data && res.data.length > 0) {
                menuRef.value?.setCheckedKeys(res.data.map(i => i.id));
            }
        });
    } catch (error: unknown) {
        console.error("未知错误", error);
    }
}

// 角色-权限关联关系保存
function handleSaveRoleAuthority() {
    if (!currentRow.value) {
        ElMessage.warning("请先选中一个角色");
        return;
    }
    let params = {
        role_id: currentRow.value.id,
        authority_ids: powerRef.value?.getCheckedKeys()
    };
    RoleApi.saveRoleAuthority(params).then(res => {
        if (res.code === 200) {
            ElMessage.success("保存成功");
        } else {
            ElMessage.error(res.msg);
        }
    });
}

// 角色-菜单 关联关系保存
function handleSaveRoleMenu() {
    if (!currentRow.value) {
        ElMessage.warning("请先选中一个角色");
        return;
    }
    let params = {
        role_id: currentRow.value.id,
        menu_ids: menuRef.value?.getCheckedKeys()
    };
    RoleApi.saveRoleMenu(params).then(res => {
        if (res.code === 200) {
            ElMessage.success("保存成功");
        } else {
            ElMessage.error(res.msg);
        }
    });
}
</script>

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
                        <el-button type="primary" @click="handleRoleConditionQuery">查询</el-button>
                        <el-button @click="handleRoleEditDialogOpen({} as Role)">新增</el-button>
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
                class="loading-box"
                @row-click="handleRoleTableRowClick">
                <el-table-column align="center" width="060" type="index" label="序号" />
                <el-table-column align="center" width="150" prop="name" label="名称" />
                <el-table-column align="center" width="120" prop="code" label="标识" show-overflow-tooltip />
                <el-table-column align="center" width="60" prop="level" label="级别">
                    <template v-slot:default="scope">1</template>
                </el-table-column>
                <el-table-column align="center" width="120" prop="scope" label="范围" />
                <el-table-column align="center" width="120" label="状态">
                    <template #default="scope">
                        <el-tag :type="scope.row.state ? 'primary' : 'danger'">
                            {{ scope.row.state ? "启用" : "禁用" }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" width="120" label="内置">
                    <template #default="scope">
                        <el-tag :type="scope.row.builtin ? 'primary' : 'danger'">
                            {{ scope.row.builtin ? "是" : "否" }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column align="center" prop="remark" label="备注" show-overflow-tooltip />
                <el-table-column align="center" label="编辑" width="120">
                    <template #default="scope">
                        <el-button link type="primary" size="small" @click="handleRoleEditDialogOpen(scope.row)">
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
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange" />
        </el-col>
        <!-- 权限 -->
        <el-col :span="4" style="padding: 10px">
            <el-text type="primary">角色权限</el-text>
            <el-divider class="divider-box" />
            <el-button link type="primary" @click="handleSaveRoleAuthority">保存角色权限</el-button>
            <el-tree
                ref="powerRef"
                :data="authority_tree"
                :props="treeDefaultProps"
                node-key="id"
                default-expand-all
                empty-text="暂无权限"
                show-checkbox />
        </el-col>
        <!-- 菜单 -->
        <el-col :span="4" style="padding: 10px">
            <el-text type="primary">角色菜单</el-text>
            <el-divider class="divider-box" />
            <el-button link type="primary" @click="handleSaveRoleMenu">保存角色菜单</el-button>
            <el-tree
                ref="menuRef"
                :data="menu_tree"
                :props="treeDefaultProps"
                node-key="id"
                default-expand-all
                empty-text="暂无菜单"
                show-checkbox />
        </el-col>
    </el-row>
    <!-- 角色编辑框 -->
    <role-edit v-model:show="edit.dialog" v-model:form="edit.form" @close="handlerConditionQuery" />
</template>

<style scoped lang="scss">
.divider-box {
    margin: 18px 0 10px 0;
}
</style>
