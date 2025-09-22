<!--
  -  Copyright 2018-2025 yangxj96
  -
  -  Licensed under the Apache License, Version 2.0 (the "License");
  -  you may not use this file except in compliance with the License.
  -  You may obtain a copy of the License at
  -
  -      http://www.apache.org/licenses/LICENSE-2.0
  -
  -  Unless required by applicable law or agreed to in writing, software
  -  distributed under the License is distributed on an "AS IS" BASIS,
  -  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  -  See the License for the specific language governing permissions and
  -  limitations under the License.
  -->

<script setup lang="ts">
import PermissionApi from "@/api/PermissionApi.ts";
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

const modify = computed(() => {
    const formValue = form.value;
    return formValue && Object.keys(formValue).length > 0;
});

// 表单
const rules = ref<FormRules>();
// refs
const formRef = useTemplateRef<FormInstance>("formRef");

// 角色保存
async function handleRoleSave() {
    if (!formRef.value) return;
    await formRef.value?.validate((valid, _) => {
        if (valid) {
            let request = modify ? PermissionApi.modifyRole : PermissionApi.createdRole;
            request(form.value).then(() => {
                ElMessage.success({
                    message: modify ? "修改角色成功" : "新增角色成功",
                    onClose() {
                        dialog.value = false;
                    }
                });
            });
        }
    });
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
            {{ `${modify ? '编辑' : '新增'}角色` }}
        </template>
        <template #default>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="auto">
                <el-form-item label="角色名称" prop="name">
                    <el-input v-model="form.name" clearable />
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
