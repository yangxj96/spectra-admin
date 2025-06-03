<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="菜单名称" prop="name">
                <el-input placeholder="请输入菜单名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary">查询</el-button>
                <el-button>重置</el-button>
                <el-button @click="handleMenuAddDialog">新增</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table v-if="table_data.length > 0" :data="table_data" height="100%" stripe default-expand-all row-key="id">
            <el-table-column align="center" type="index" />
            <el-table-column align="center" prop="name" label="名称" />
            <el-table-column align="center" prop="icon" label="图标">
                <template v-slot:default="scope">
                    <icons :name="scope.row.icon" />
                </template>
            </el-table-column>
            <el-table-column align="center" prop="path" label="请求路径" />
            <el-table-column align="center" prop="component" label="组件路径" />
            <el-table-column align="center" prop="layout" label="布局" />
            <el-table-column align="center" prop="sort" label="排序" />
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
    </el-row>

    <!-- 新增或编辑 -->
    <el-dialog
        v-model="menu.dialog"
        append-to-body
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        :title="(menu.modify ? '编辑' : '新增') + '菜单'"
        width="30vw">
        <template #default>
            <el-form v-loading="menu.loading" label-width="auto" @submit.prevent>
                <el-form-item label="姓名" prop="username">
                    <el-input placeholder="请输入姓名" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button :disabled="menu.loading" @click="() => (menu.dialog = false)">取消</el-button>
            <el-button :disabled="menu.loading" type="primary" @click="handleUserSave">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import MenuApi from "@/api/MenuApi.ts";
import { ElMessage, ElMessageBox, type FormRules } from "element-plus";

const table_data = ref<Menu[]>([]);

// 新增或编辑
const menu = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as Menu,
    rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }]
    } as FormRules
});

onMounted(() => {
    MenuApi.tree().then((res: IResult<Menu[]>) => {
        if (res.code === 200 && res.data) {
            table_data.value = res.data;
        }
    });
});

// 表行修改按钮被单击
function handleTableItemModify(row: Menu) {
    menu.modify = true;
    menu.form = row;
    menu.dialog = true;
}

// 表行删除按钮被单击
function handleTableItemDelete(row: Menu) {
    ElMessageBox.confirm(`是否要删除[${row.name}]`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "error"
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
function handleUserSave() {
    console.log(`保存:`, menu.form);
    menu.loading = true;
    let i = setInterval(() => {
        menu.loading = false;
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
