<script setup lang="ts">
import { computed, ref, useTemplateRef } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { treeDefaultProps } from "@/utils/Config.ts";
import OrganizationApi from "@/api/OrganizationApi.ts";
import icons from "@/components/Icons/index.vue";
import DictSelect from "@/components/DictSelect/index.vue";

// model<
const dialog = defineModel("show", {
    required: true,
    default: false
});
const form = defineModel("form", {
    required: false,
    default: {} as Organization
});
const tree = defineModel("tree", {
    required: true,
    default: [] as OrganizationTree[]
});
// 定义响应方法
const emits = defineEmits(["close"]);
// 获取是否为修改
const modify = computed(() => {
    return !!form.value.id;
});
// 表单
const rules = ref<FormRules>({
    name: [{ required: true, message: "请输入部门名称", trigger: "blur" }],
    type: [{ required: true, message: "请选择部门类型", trigger: "blur" }]
});
// refs
const formRef = useTemplateRef<FormInstance>("formRef");

// 处理关闭
function handleCurrentDialogClose() {
    dialog.value = false;
    emits("close");
}

// 新增或编辑
async function handleOrganizationSave() {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        let request = modify.value ? OrganizationApi.modify : OrganizationApi.created;
        let res = await request(form.value);
        if (res.code === 200) {
            ElMessage.success({
                message: modify.value ? "修改组织机构成功" : "新增组织机构成功",
                appendTo: ".box-content",
                onClose() {
                    handleCurrentDialogClose();
                }
            });
        } else {
            ElMessage.error({
                message: res.msg,
                appendTo: ".box-content"
            });
        }
    } catch (error) {
        console.log(error);
    }
}
</script>

<template>
    <el-dialog
        v-model="dialog"
        class="loading-box"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        width="30vw">
        <template #header>
            <icons name="icon-edit" />
            {{ (modify ? "编辑" : "新增") + "部门" }}
        </template>
        <template #default>
            <el-form
                ref="formRef"
                :rules="rules"
                :model="form"
                label-width="auto"
                style="padding: 20px"
                @submit.prevent>
                <el-form-item v-if="modify" label="主键" prop="id">
                    <el-text type="info">{{ form.id }}</el-text>
                </el-form-item>
                <el-form-item v-if="modify" label="编码" prop="code">
                    <el-text type="info">{{ form.code }}</el-text>
                </el-form-item>
                <el-form-item label="父级" prop="pid">
                    <el-tree-select
                        v-model="form.pid"
                        default-expand-all
                        check-strictly
                        :data="tree"
                        node-key="id"
                        clearable
                        :props="treeDefaultProps" />
                </el-form-item>
                <el-form-item label="名称" prop="name">
                    <el-input v-model="form.name" clearable placeholder="请输入部门名称" />
                </el-form-item>
                <el-form-item label="类型" prop="type">
                    <dict-select v-model="form.type" dict_code="sys_organization_type" placeholder="请选择部门类型" />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="5" clearable placeholder="请输入相关备注" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="handleCurrentDialogClose">取消</el-button>
            <el-button type="primary" @click="handleOrganizationSave()">确定</el-button>
        </template>
    </el-dialog>
</template>
