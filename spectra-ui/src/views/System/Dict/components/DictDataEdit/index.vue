<script setup lang="ts">
import DictApi from "@/api/DictApi";
import { ElMessage, type FormInstance } from "element-plus";
import { reactive } from "vue";
import _ from "lodash";

const props = defineProps<{
    row?: DictData;
    group?: DictGroup;
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

// 弹出框配置
const dialog = ref({
    visible: true,
    loading: false,
    title: (has_edit ? "编辑" : "新增") + "字典数据"
});

// 定义字典组编辑对话框的状态和表单数据
const edit = reactive({
    rules: {
        gid: [{ required: true, message: "请选择所属字典组", trigger: "blur" }],
        label: [
            { required: true, message: "请输入字典项名称", trigger: "blur" },
            { min: 2, max: 20, message: "长度在 2 到 20 个字符", trigger: "blur" }
        ],
        value: [
            { required: true, message: "请输入字典项值", trigger: "blur" },
            { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
        ],
        sort: [{ required: true, message: "请输入排序值", trigger: "blur" }],
        state: [{ required: true, message: "请选择字典状态", trigger: "blur" }]
    },
    form: {} as DictData
});

onMounted(() => {
    handleInitData();
    edit.form = has_edit ? _.cloneDeep(props.row || edit.form) : ({ state: 0, sort: 999 } as DictData);
    if (!has_edit && props.group) {
        edit.form.gid = props.group.id;
    }
});

// 初始化数据
function handleInitData() {
    DictApi.getTypesGroupTree()
        .then((res: IResult<DictGroup[]>) => {
            if (res.code === 200) {
                gropus.value = res.data || [];
            } else {
                ElMessage.error(res.msg || "获取字典组列表失败");
            }
        })
        .catch(() => {
            ElMessage.error("获取字典组列表失败");
        });
}

// 保存
function handleSaveDictGroup() {
    if (!editForm.value) return;
    editForm.value?.validate(valid => {
        if (!valid) {
            ElMessage.error("请检查必填内容");
            return;
        }
        dialog.value.loading = true;
        let request = has_edit ? DictApi.modifyData : DictApi.createData;
        request(edit.form)
            .finally(() => {
                dialog.value.loading = false;
            })
            .then((res: IResult) => {
                if (res.code === 200) {
                    ElMessage.success({
                        message: "保存成功",
                        duration: 1000,
                        onClose() {
                            emit("close");
                        }
                    });
                } else {
                    ElMessage.error(res.msg || "保存失败");
                }
            });
    });
}
</script>

<template>
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500">
        <template #default>
            <el-form
                ref="editForm"
                v-loading="dialog.loading"
                :model="edit.form"
                :rules="edit.rules"
                label-width="auto">
                <el-form-item v-if="has_edit" label="主键ID">
                    <p>{{ edit.form.id }}</p>
                </el-form-item>
                <el-form-item label="所属字典组" prop="gid">
                    <el-tree-select
                        v-model="edit.form.gid"
                        default-expand-all
                        check-strictly
                        :data="gropus"
                        node-key="id"
                        :props="treeProps" />
                </el-form-item>
                <el-form-item label="字典标签" prop="label">
                    <el-input v-model="edit.form.label" placeholder="请输入字典标签" />
                </el-form-item>
                <el-form-item label="字典值" prop="value">
                    <el-input v-model="edit.form.value" placeholder="请输入字典值" />
                </el-form-item>
                <el-form-item label="排序" prop="sort">
                    <el-input-number
                        v-model="edit.form.sort"
                        :min="1"
                        :max="999"
                        placeholder="请输入字典排序"
                        style="width: 100%" />
                </el-form-item>
                <el-form-item label="字典状态" prop="state">
                    <dict-select v-model="edit.form.state" dict_code="sys_common_state" placeholder="请选择字典状态" />
                </el-form-item>
                <el-form-item label="字典描述">
                    <el-input v-model="edit.form.remark" type="textarea" placeholder="请输入字典描述" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <div class="dialog-footer">
                <el-button :loading="dialog.loading" @click="$emit('close')">取消</el-button>
                <el-button :loading="dialog.loading" type="primary" @click="handleSaveDictGroup">确认</el-button>
            </div>
        </template>
    </el-dialog>
</template>
