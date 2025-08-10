<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from "vue";
import MenuApi from "@/api/MenuApi.ts";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import _ from "lodash";
import IconPicker from "@/components/IconPicker/index.vue";

const menuForm = useTemplateRef<FormInstance>("ruleFormRef");
const table_data = ref<Menu[]>([]);

// 新增或编辑
const menu = reactive({
    dialog: false,
    modify: false,
    form: {} as Menu,
    rules: {
        name: [{ required: true, message: "请输入菜单名称", trigger: "blur" }],
        icon: [{ required: true, message: "请选择菜单图标", trigger: "blur" }],
        path: [{ required: true, message: "请输入菜单路径", trigger: "blur" }],
        component: [{ required: true, message: "请输入组件路径", trigger: "blur" }],
        sort: [{ required: true, message: "请输入排序", trigger: "blur" }]
    } as FormRules
});

const ready = ref(false);

onMounted(() => {
    handleCriteriaQuery();
    ready.value = true;
});

// 初始化数据
function handleCriteriaQuery() {
    MenuApi.tree().then((res: IResult<Menu[]>) => {
        if (res.code === 200 && res.data) {
            table_data.value = res.data;
        }
    });
}

// 表行修改按钮被单击
function handleTableItemModify(row: Menu) {
    console.log(`菜单`, row);
    menu.modify = true;
    menu.form = _.cloneDeep(row);
    menu.dialog = true;
}

// 表行删除按钮被单击
function handleTableItemDelete(row: Menu) {
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
function handleMenuAddDialog() {
    menu.modify = false;
    menu.form = {} as Menu;
    menu.dialog = true;
}

// 新增或编辑
async function handleMenuSave() {
    if (!menuForm.value) return;
    await menuForm.value?.validate((valid, _) => {
        if (valid) {
            let request = menu.modify ? MenuApi.modify : MenuApi.created;
            request(menu.form).then(() => {
                ElMessage.success({
                    message: menu.modify ? "修改菜单成功" : "新增菜单成功",
                    onClose() {
                        menu.dialog = false;
                        handleCriteriaQuery();
                    }
                });
            });
        }
    });
}
</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="菜单名称" prop="name">
                <el-input placeholder="请输入菜单名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleCriteriaQuery">查询</el-button>
                <el-button>重置</el-button>
                <el-button @click="handleMenuAddDialog">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table
            v-if="table_data.length > 0"
            :data="table_data"
            height="100%"
            stripe
            default-expand-all
            row-key="id"
            class="loading-box">
            <el-table-column align="center" type="index" />
            <el-table-column align="center" prop="name" label="名称" />
            <el-table-column align="center" prop="icon" label="图标">
                <template #default="scope">
                    <icons :name="scope.row.icon" />
                </template>
            </el-table-column>
            <el-table-column align="center" prop="path" label="请求路径" />
            <el-table-column align="center" prop="component" label="组件路径" />
            <el-table-column align="center" prop="layout" label="布局" />
            <el-table-column align="center" prop="sort" label="排序" />
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
    </el-row>
    <!-- 新增或编辑 -->
    <el-dialog
        v-if="ready"
        v-model="menu.dialog"
        class="loading-box"
        :append-to="'.box-content'"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        :title="(menu.modify ? '编辑' : '新增') + '菜单'"
        width="30vw">
        <template #default>
            <el-form
                ref="ruleFormRef"
                :rules="menu.rules"
                :model="menu.form"
                label-width="auto"
                style="padding: 20px"
                @submit.prevent>
                <el-form-item v-if="menu.modify" label="数据ID" prop="id">
                    <el-input v-model="menu.form.id" disabled clearable placeholder="请输入菜单名称" />
                </el-form-item>
                <el-form-item label="上级菜单" prop="pid">
                    <el-tree-select
                        v-model="menu.form.pid"
                        :data="table_data"
                        placeholder="请选择菜单父级"
                        check-strictly
                        default-expand-all
                        node-key="id"
                        append-to=".box-content"
                        :props="{ label: 'name' }"
                        :render-after-expand="false" />
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="menu.form.name" clearable placeholder="请输入菜单名称" />
                </el-form-item>
                <el-form-item label="图标" prop="icon">
                    <icon-picker v-model="menu.form.icon" />
                </el-form-item>
                <el-form-item label="路径" prop="path">
                    <el-input v-model="menu.form.path" clearable placeholder="请输入路径" />
                </el-form-item>
                <el-form-item label="组件" prop="component">
                    <el-input v-model="menu.form.component" clearable placeholder="请输入组件路径" />
                </el-form-item>
                <el-form-item label="布局" prop="layout">
                    <el-select v-model="menu.form.layout" clearable placeholder="请输入布局" append-to=".box-content">
                        <el-option label="默认布局" value="layout" />
                    </el-select>
                </el-form-item>
                <el-form-item label="排序" prop="sort">
                    <el-input-number
                        v-model="menu.form.sort"
                        :min="0"
                        :max="999"
                        placeholder="请输入排序"
                        style="width: 100%" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="() => (menu.dialog = false)">取消</el-button>
            <el-button type="primary" @click="handleMenuSave()">确定</el-button>
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
