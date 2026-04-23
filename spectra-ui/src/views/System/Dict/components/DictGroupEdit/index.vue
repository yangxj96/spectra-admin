<script setup lang="ts">
import { type FormInstance } from "element-plus";
import { onMounted, reactive, ref, useTemplateRef } from "vue";

import { dictApi } from "@/api/system/dict.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import DictSelect from "@/components/DictSelect/index.vue";
import { MessageUtils } from "@/utils/message-utils.ts";

const props = defineProps<{
    row?: DictGroup;
}>();

const emit = defineEmits(["close"]);

// 字典组表单数据
const editForm = useTemplateRef<FormInstance>("editForm");

// 树形props配置
const treeProps = { children: "children", label: "name", value: "id" };

// 字典组列表
const gropus = ref<DictGroup[]>([]);

// 编辑标识,是否为编辑数据
const has_edit = props.row?.id && props.row.id !== "";

// 定义字典组编辑对话框的状态和表单数据
const edit = reactive({
    visible: true,
    title: (has_edit ? "编辑" : "新增") + "字典组",
    rules: {
        name: [
            { required: true, message: "请输入字典名称", trigger: "blur" },
            { min: 2, max: 30, message: "长度在 2 到 30 个字符", trigger: "blur" }
        ],
        code: [
            { required: true, message: "请输入字典编码", trigger: "blur" },
            { min: 2, max: 30, message: "长度在 2 到 30 个字符", trigger: "blur" }
        ],
        state: [{ required: true, message: "请选择字典状态", trigger: "blur" }]
    },
    form: {} as DictGroup
});

onMounted(() => {
    handleInitData();
    edit.form = has_edit ? JSON.parse(JSON.stringify(props.row || edit.form)) : ({ state: 0 } as DictGroup);
});

// 初始化数据
const handleInitData = async () => {
    gropus.value = await dictApi.getTypesGroupTree();
};

// 保存字典组
const handleSaveDictGroup = () => {
    if (!editForm.value) return;
    editForm.value?.validate(async valid => {
        if (!valid) {
            MessageUtils.error("请检查必填内容");
            return;
        }
        if (has_edit) {
            await dictApi.updateGroup(edit.form);
        } else {
            await dictApi.createGroup(edit.form);
        }
        MessageUtils.success("保存成功", () => {
            emit("close");
        });
    });
};
</script>

<template>
    <el-dialog v-model="edit.visible" width="500" class="loading-box">
        <template #header>
            <ComponentsIcons name="icon-edit" />
            {{ edit.title }}
        </template>
        <template #default>
            <el-form ref="editForm" :model="edit.form" :rules="edit.rules" label-width="auto">
                <el-form-item v-if="has_edit" label="主键ID">
                    <p>{{ edit.form.id }}</p>
                </el-form-item>
                <el-form-item label="上级字典组" prop="pid">
                    <el-tree-select
                        v-model="edit.form.pid"
                        clearable
                        check-strictly
                        default-expand-all
                        :data="gropus"
                        node-key="id"
                        append-to=".box-content"
                        :props="treeProps" />
                </el-form-item>
                <el-form-item label="字典名称" prop="name">
                    <el-input v-model="edit.form.name" clearable placeholder="请输入字典名称" />
                </el-form-item>
                <el-form-item label="字典编码" prop="code">
                    <el-input v-model="edit.form.code" clearable placeholder="请输入字典编码" />
                </el-form-item>
                <el-form-item label="字典状态" prop="state">
                    <DictSelect v-model="edit.form.state" dict_code="sys_common_state" placeholder="请选择字典状态" />
                </el-form-item>
                <el-form-item label="字典描述">
                    <el-input v-model="edit.form.remark" clearable type="textarea" placeholder="请输入字典描述" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="$emit('close')">取消</el-button>
                <el-button type="primary" @click="handleSaveDictGroup">确认</el-button>
            </div>
        </template>
    </el-dialog>
</template>
