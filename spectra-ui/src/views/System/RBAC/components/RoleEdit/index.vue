<script setup lang="ts">
import { type FormInstance, type FormRules } from "element-plus";
import { computed, useTemplateRef } from "vue";

import { roleApi } from "@/api/auth/role.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

// model
const open = defineModel("show", {
    required: true,
    default: false
});

const form = defineModel("form", {
    required: false,
    default: {} as Role
});

// 定义响应方法
const emits = defineEmits(["close"]);

// refs
const formRef = useTemplateRef<FormInstance>("formRef");

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

// 处理关闭
const handleClose = () => {
    open.value = false;
    emits("close");
};

// 角色保存
const handleSave = async () => {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        if (modify.value) {
            await roleApi.update(form.value);
        } else {
            await roleApi.create(form.value);
        }
        MessageUtils.success(modify.value ? "修改角色成功" : "新增角色成功", handleClose);
    } catch (error) {
        console.error(error);
        MessageUtils.error(error);
    }
};
</script>

<template>
    <el-drawer v-model="open" :modal="true" modal-penetrable destroy-on-close @close="handleClose">
        <template #header>
            <div>
                <components-icons name="icon-edit" />
                {{ `${modify ? "编辑" : "新增"}角色` }}
            </div>
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
            <el-button @click="handleClose">关闭</el-button>
            <el-button @click="handleSave">提交</el-button>
        </template>
    </el-drawer>
</template>
