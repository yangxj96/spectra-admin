<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="部门名称" prop="name">
                <el-input placeholder="请输入部门名称" clearable />
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
        <el-table v-if="table_data.length > 0" :data="table_data" height="100%" stripe default-expand-all row-key="id">
            <el-table-column align="center" type="index" />
            <el-table-column align="center" prop="name" label="名称" />
            <el-table-column align="center" prop="type" label="类型">
                <template #default="scope">
                    <el-tag>{{ scope.row.type }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column align="center" prop="code" label="代码" />
            <el-table-column align="center" prop="sort" label="排序" />
            <el-table-column align="center" prop="remark" label="说明" show-overflow-tooltip />
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
        v-model="edit.dialog"
        :append-to="'.box-content'"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        :title="(edit.modify ? '编辑' : '新增') + '部门'"
        width="30vw">
        <template #default>
            <el-form
                ref="formRef"
                v-loading="edit.loading"
                :rules="edit.rules"
                :model="edit.form"
                label-width="auto"
                style="padding: 20px"
                @submit.prevent>
                <el-form-item v-if="edit.modify" label="数据ID" prop="id">
                    <el-input v-model="edit.form.id" disabled clearable placeholder="请输入菜单名称" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button :disabled="edit.loading" @click="() => (edit.dialog = false)">取消</el-button>
            <el-button :disabled="edit.loading" type="primary" @click="handleMenuSave()">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import _ from "lodash";

const formRef = useTemplateRef<FormInstance>("formRef");
const table_data = ref<Dept[]>([]);

// 新增或编辑
const edit = reactive({
    dialog: false,
    modify: false,
    loading: false,
    form: {} as Dept,
    rules: {} as FormRules
});

const ready = ref(false);

onMounted(() => {
    ready.value = true;
    handleCriteriaQuery();
});

// 初始化数据
function handleCriteriaQuery() {
    let data = [] as Dept[];
    for (let i = 0; i < 10; i++) {
        let datum = {
            id: "11111111111" + i,
            name: "部门" + i,
            type: "总公司" + i,
            code: "BMDM-0" + i,
            sort: i,
            remark: "我是我是备注我是备注我是备注我是备注我是备注我是备注备注",
            children: [] as Dept
        };
        for (let i = 0; i < 3; i++) {
            datum.children.push({
                id: "211111111111" + i,
                name: "二级部门" + i,
                type: "二级总公司" + i,
                code: "22BMDM-0" + i,
                sort: i,
                remark: "我是我是备注我是备注我是备注我是备注我是备注我是备注备注"
            });
        }
        data.push(datum);
    }
    table_data.value = data;
}

// 表行修改按钮被单击
function handleTableItemModify(row: Dept) {
    edit.modify = true;
    edit.form = _.cloneDeep(row);
    edit.dialog = true;
}

// 表行删除按钮被单击
function handleTableItemDelete(row: Dept) {
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
    edit.modify = false;
    edit.form = {} as Dept;
    edit.dialog = true;
}

// 新增或编辑
async function handleMenuSave() {}
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
