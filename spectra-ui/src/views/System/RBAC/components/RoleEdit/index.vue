<script setup lang="ts">
import RoleApi from "@/api/RoleApi.ts";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { useTemplateRef } from "vue";

// model
const dialog = defineModel("show", {
    required: true,
    default: false
});

const form = defineModel("form", {
    required: false,
    default: {} as Role
});

// 定义响应方法
const emits = defineEmits(["close"]);

// 获取是否为修改
const modify = computed(() => {
    return !!form.value.id;
});

// 路由规则
const rules = {
    name: [
        { required: true, message: "请输入角色名称", trigger: "blur" },
        { min: 2, max: 20, message: "角色名称长度需要在2-20字符范围内", trigger: "blur" }
    ],
    scope: [{ required: true, message: "请选择角色分类", trigger: "change" }],
    state: [{ required: true, message: "请选择角色状态", trigger: "change" }]
} as FormRules;

// refs
const formRef = useTemplateRef<FormInstance>("formRef");

// 处理关闭
function handleCurrentDialogClose() {
    dialog.value = false;
    emits("close");
}

// 角色保存
async function handleRoleSave() {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        let request = modify.value ? RoleApi.modify : RoleApi.created;
        await request(form.value);
        ElMessage.success({
            message: modify.value ? "修改角色成功" : "新增角色成功",
            onClose() {
                handleCurrentDialogClose();
            }
        });
    } catch (error) {
        console.error(error);
    }
}
</script>

<template>
    <el-dialog
        v-model="dialog"
        width="30%"
        class="loading-box"
        :show-close="false"
        :destroy-on-close="true"
        :close-on-click-modal="false"
        :close-on-press-escape="false">
        <template #header>
            <icons name="icon-edit" />
            {{ `${modify ? "编辑" : "新增"}角色` }}
        </template>
        <template #default>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="auto">
                <el-form-item v-if="modify" label="ID" prop="name">
                    <el-text type="info">{{ form.id }}</el-text>
                </el-form-item>
                <el-form-item v-if="modify" label="编码" prop="name">
                    <el-text type="info">{{ form.code }}</el-text>
                </el-form-item>
                <el-form-item label="角色名称" prop="name">
                    <el-input v-model="form.name" show-word-limit clearable />
                </el-form-item>
                <el-form-item label="角色范围" prop="scope">
                    <el-select v-model="form.scope" clearable append-to=".box-content">
                        <el-option value="全局" label="全局" />
                        <el-option value="本级及下级" label="本级及下级" />
                        <el-option value="本级" label="本级" />
                    </el-select>
                </el-form-item>
                <el-form-item label="是否启用" prop="state">
                    <el-select v-model="form.state" clearable append-to=".box-content">
                        <el-option :value="true" label="是" />
                        <el-option :value="false" label="否" />
                    </el-select>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" clearable />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="() => (dialog = false)">关闭</el-button>
            <el-button @click="handleRoleSave">提交</el-button>
        </template>
    </el-dialog>
</template>
