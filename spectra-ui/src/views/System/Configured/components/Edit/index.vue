<script setup lang="ts">
import { useTemplateRef } from "vue";

import { configuredApi } from "@/api/system/configured.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import DictSelect from "@/components/DictSelect/index.vue";
import { configuredConverter } from "@/converter/configured-converter.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import type { ElForm, FormInstance } from "element-plus";

// 是否显示
const show = defineModel("show", {
    type: Boolean,
    required: true,
    default: false
});

// 表单
const form = defineModel<ConfiguredForm>("form", {
    required: true
});

const formRef = useTemplateRef<FormInstance>("formRef");

// 定义响应方法
const emits = defineEmits(["close"]);

const handleClose = () => {
    show.value = false;
    emits("close");
};

const handleConfiguredSave = async () => {
    if (!formRef.value) return;
    try {
        await formRef.value.validate();
        await configuredApi.upload(configuredConverter.toDTO(form.value));
        MessageUtils.success("修改配置成功", handleClose);
    } catch (error) {
        console.error(error);
    }
};
</script>

<template>
    <!-- 配置编辑 -->
    <el-drawer v-model="show" :modal="true" modal-penetrable destroy-on-close @close="handleClose">
        <template #header>
            <div>
                <ComponentsIcons name="icon-edit" />
                编辑系统配置
            </div>
        </template>

        <template #default>
            <el-form ref="formRef" :model="form" label-width="auto" @submit.prevent>
                <el-form-item label="ID" prop="id">
                    <el-text>{{ form.id }}</el-text>
                </el-form-item>
                <el-form-item label="配置键" prop="key">
                    <el-text>{{ form.key }}</el-text>
                </el-form-item>
                <el-form-item label="配置值" prop="value">
                    <el-switch
                        v-if="form.type === 'BOOL'"
                        v-model="form.value"
                        active-value="true"
                        inactive-value="false" />
                    <DictSelect v-else-if="form.type === 'SELECT'" v-model="form.value" :dict_code="form.dict_code" />
                    <el-input v-else v-model="form.value" placeholder="请输入配置值" />
                </el-form-item>
                <el-form-item label="备注" prop="remarks">
                    <el-input v-model="form.remarks" type="textarea" :rows="5" placeholder="请输入配置说明" />
                </el-form-item>
            </el-form>
        </template>

        <template #footer>
            <el-button @click="handleClose">取消</el-button>
            <el-button @click="handleConfiguredSave" type="primary">确定</el-button>
        </template>
    </el-drawer>
</template>
