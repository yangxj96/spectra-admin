<script setup lang="ts">
import { computed, ref, useTemplateRef } from "vue";
import { type FormInstance, type FormRules } from "element-plus";
import { treeDefaultProps } from "@/utils/default-config.ts";
import { departmentApi } from "@/api/user/organization.ts";
import DictSelect from "@/components/DictSelect/index.vue";
import { MessageUtils } from "@/utils/message-utils.ts";
import RegionSelectLazy from "@/components/RegionSelectLazy/index.vue";

// model<
const dialog = defineModel("show", {
    required: true,
    default: false
});
const form = defineModel("form", {
    required: false,
    default: {} as Department
});
const tree = defineModel("tree", {
    required: true,
    default: [] as DepartmentTree[]
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
    region_id: [{ required: true, message: "请选择所属行政区划", trigger: "blur" }],
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
        const request = modify.value ? departmentApi.modify : departmentApi.created;
        const res = await request(form.value);
        if (res.code === 200) {
            MessageUtils.success(modify.value ? "修改组织机构成功" : "新增组织机构成功", () => {
                handleCurrentDialogClose();
            });
        } else {
            MessageUtils.error(res.msg);
        }
    } catch (error) {
        console.log(error);
    }
}
</script>

<template>
    <el-drawer
        v-model="dialog"
        :modal="true"
        class="loading-box"
        modal-penetrable
        destroy-on-close
        @close="handleCurrentDialogClose">
        <template #header>
            <div>
                <components-icons name="icon-edit" />
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
                    <region-select-lazy v-model="form.region_id" :name="form.region_name" />
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
    </el-drawer>
</template>
