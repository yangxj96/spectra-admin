<script setup lang="ts">
import { ElTree } from "element-plus";
import { onMounted, reactive, ref, useTemplateRef } from "vue";

import { authorityApi } from "@/api/auth/authority.ts";
import { roleApi } from "@/api/auth/role.ts";
import { menuApi } from "@/api/system/menu.ts";
import { roleConverter } from "@/converter/role-converter.ts";
import UseTable from "@/hooks/use-table.ts";
import { treeDefaultProps } from "@/utils/default-config.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import RoleEdit from "./components/RoleEdit/index.vue";

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
const edit = reactive<{
    dialog: boolean;
    form: RoleForm;
}>({
    dialog: false,
    form: roleConverter.createForm()
});
const currentRow = ref<RolePageVO>();

const { handlerConditionQuery, handleCurrentChange, handleSizeChange, pagination, table_data } = UseTable<RolePageVO>(
    roleApi.page,
    condition.value
);

onMounted(() => {
    handleInitData();
});

// 初始化数据
const handleInitData = async () => {
    menu_tree.value = await menuApi.tree();
    authority_tree.value = await authorityApi.tree();
};

const handleRoleAdd = () => {
    edit.form = roleConverter.createForm();
    edit.dialog = true;
};

const handleRoleEdit = (row: RolePageVO) => {
    edit.form = roleConverter.toForm(row);
    edit.dialog = true;
};

// 角色删除
const handleRoleDelete = (row: RolePageVO) => {
    MessageUtils.box.confirm(`是否要删除[${row.name}]`, "提示").then(async () => {
        await roleApi.delete(row.id);
        MessageUtils.success("删除成功");
        await handlerConditionQuery();
    });
};

// 条件查询
const handleRoleConditionQuery = () => {
    cleanTreeCheckState();
    handlerConditionQuery();
};

// 清理右边两棵树的选中状态
const cleanTreeCheckState = () => {
    currentRow.value = undefined;
    if (authority_tree.value)
        for (const item of authority_tree.value) {
            powerRef.value?.setChecked(item.id, false, true);
        }

    if (menu_tree.value)
        for (const item of menu_tree.value) {
            menuRef.value?.setChecked(item.id, false, true);
        }
};

// 角色列表行被单机
const handleRoleTableRowClick = async (row: RolePageVO) => {
    if (currentRow.value && currentRow.value.id && currentRow.value.id === row.id) return;
    try {
        currentRow.value = row;
        cleanTreeCheckState();
        // 权限部分
        const roleAuthority = await roleApi.getRoleAuthority(row.id);
        const roleAuthorityIds = roleAuthority.map(i => i.id);
        powerRef.value?.setCheckedKeys(roleAuthorityIds);

        // 菜单部分
        const roleMenu = await roleApi.getRoleMenu(row.id);
        menuRef.value?.setCheckedKeys(roleMenu.map(i => i.id));
    } catch (error: unknown) {
        console.error("未知错误", error);
    }
};

// 角色-权限关联关系保存
const handleSaveRoleAuthority = async () => {
    if (!currentRow.value) {
        MessageUtils.warning("请先选中一个角色");
        return;
    }
    const params = {
        role_id: currentRow.value.id,
        authority_ids: powerRef.value?.getCheckedKeys()
    };
    await roleApi.saveRoleAuthority(params);
    MessageUtils.success("保存成功");
};

// 角色-菜单 关联关系保存
const handleSaveRoleMenu = async () => {
    if (!currentRow.value) {
        MessageUtils.warning("请先选中一个角色");
        return;
    }
    const params = {
        role_id: currentRow.value.id,
        menu_ids: menuRef.value?.getCheckedKeys()
    };
    await roleApi.saveRoleMenu(params);
    MessageUtils.success("保存成功");
};
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
                        <el-button type="primary" @click="handleRoleConditionQuery()">查询</el-button>
                        <el-button @click="handleRoleAdd()">新增</el-button>
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
                @row-click="handleRoleTableRowClick">
                <el-table-column align="center" width="060" type="index" label="序号" />
                <el-table-column align="center" width="150" prop="name" label="名称" />
                <el-table-column align="center" width="120" prop="code" label="标识" show-overflow-tooltip />
                <el-table-column align="center" width="120" prop="scope" label="范围" />
                <el-table-column align="center" width="120" label="是否启用">
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
                        <el-button link type="primary" size="small" @click="handleRoleEdit(scope.row)">编辑</el-button>
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
            <ElTree
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
            <ElTree
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
    <RoleEdit
        v-if="edit.dialog"
        v-model:open="edit.dialog"
        v-model:form="edit.form"
        @close="handleRoleConditionQuery" />
</template>

<style scoped lang="scss">
.divider-box {
    margin: 18px 0 10px 0;
}
</style>
