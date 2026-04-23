<script setup lang="ts">
import { type FormInstance, type FormRules } from "element-plus";
import { computed, useTemplateRef } from "vue";

import { departmentApi } from "@/api/user/organization.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import DictSelect from "@/components/DictSelect/index.vue";
import RegionSelectLazy from "@/components/RegionSelectLazy/index.vue";
import { deptConverter } from "@/converter/dept-converter.ts";
import { treeDefaultProps } from "@/utils/default-config.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

// model<
const dialog = defineModel<boolean>("show", {
    required: true,
    default: false
});
const form = defineModel<DepartmentForm>("form", {
    required: true
});

const tree = defineModel<DepartmentTreeVO[]>("tree", {
    required: true
});

// 定义响应方法
const emits = defineEmits(["close"]);

// 获取是否为修改
const modify = computed(() => {
    return !!form.value.id;
});

// 表单
const rules: FormRules<DepartmentForm> = {
    name: [{ required: true, message: "请输入部门名称", trigger: "blur" }],
    region_id: [{ required: true, message: "请选择所属行政区划", trigger: "blur" }],
    type: [{ required: true, message: "请选择部门类型", trigger: "blur" }]
};

// refs
const formRef = useTemplateRef<FormInstance>("formRef");

// 处理关闭
function handleClose() {
    dialog.value = false;
    emits("close");
}

// 新增或编辑
const handleOrganizationSave = async () => {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        if (modify.value) {
            await departmentApi.update(deptConverter.toDTO(form.value));
        } else {
            await departmentApi.create(deptConverter.toDTO(form.value));
        }
        MessageUtils.success(modify.value ? "修改组织机构成功" : "新增组织机构成功", handleClose);
    } catch (error) {
        console.log(error);
    }
};
</script>

<template>
    <el-drawer v-model="dialog" :modal="true" class="loading-box" @close="handleClose">
        <template #header>
            <div>
                <ComponentsIcons name="icon-edit" />
                {{ (modify ? "编辑" : "新增") + "部门" }}
            </div>
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
                <el-form-item label="区域" prop="region_id">
                    <RegionSelectLazy v-model="form.region_id" :name="form.region_name" />
                </el-form-item>
                <el-form-item label="类型" prop="type">
                    <DictSelect v-model="form.type" dict_code="sys_organization_type" placeholder="请选择部门类型" />
                </el-form-item>
                <el-form-item label="排序" prop="sort">
                    <el-input-number v-model="form.sort" placeholder="请输入排序" style="width: 100%" />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="5" clearable placeholder="请输入相关备注" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="handleClose()">取消</el-button>
            <el-button type="primary" @click="handleOrganizationSave()">确定</el-button>
        </template>
    </el-drawer>
</template>
