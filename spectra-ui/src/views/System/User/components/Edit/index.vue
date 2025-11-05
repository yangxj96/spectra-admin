<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { treeDefaultProps } from "@/utils/Config.ts";
import * as VerifyRules from "@/utils/VerifyRules.ts";
import UserApi from "@/api/UserApi.ts";
import OrganizationApi from "@/api/OrganizationApi.ts";
import RoleApi from "@/api/RoleApi.ts";
import icons from "@/components/Icons/index.vue";
import DictSelect from "@/components/DictSelect/index.vue";

// 定义Model
const form = defineModel("form", {
    required: false,
    default: {} as User
});

const open = defineModel<boolean>("open", { required: true, default: false });

// 定义响应方法
const emits = defineEmits(["close"]);

// 表单规则
const rules = {
    name: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    email: [
        { required: true, message: "请输入邮箱", trigger: "blur" },
        { validator: VerifyRules.email, message: "请输入正确的邮箱", trigger: "blur" }
    ],
    state: [{ required: true, message: "请选择状态", trigger: "blur" }],
    organization_id: [{ required: true, message: "请选择所属组织", trigger: "blur" }]
} as FormRules;

// 数据
const roles = ref<Role[]>();
const organization_tree = ref<OrganizationTree[]>();

// 组件
const formRef = useTemplateRef<FormInstance>("formRef");

onMounted(() => {
    let request = [RoleApi.list(), OrganizationApi.tree()];
    Promise.all(request).then(([role, org]) => {
        roles.value = role!.data as Role[];
        organization_tree.value = org!.data as OrganizationTree[];
    });
});

// 处理关闭
function handleCurrentDialogClose() {
    open.value = false;
    emits("close");
}

// 新增或编辑用户
async function handleUserSave() {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        let request = form.value.id ? UserApi.modify : UserApi.created;
        await request(form.value!);
        ElMessage.success({
            message: form.value.id ? "修改用户成功" : "新增用户成功",
            onClose: handleCurrentDialogClose
        });
    } catch (error) {
        // 输出到控制台就好了,不需要进行提示
        console.error(error);
    }
}
</script>

<template>
    <!-- 新增或编辑 -->
    <el-dialog
        v-model="open"
        class="loading-box"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        width="30vw">
        <template #header>
            <icons name="icon-edit" />
            {{ (form.id ? "编辑" : "新增") + "用户" }}
        </template>
        <template #default>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="auto" @submit.prevent>
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="form.name" placeholder="请输入姓名" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="form.email" placeholder="请输入邮箱">
                        <template #suffix>
                            <el-tooltip effect="dark" content="同时也作为登录账号" placement="right">
                                <icons name="icon-hint" style="margin-left: 10px; width: 1.4em; height: 1.4em" />
                            </el-tooltip>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item label="状态" prop="state">
                    <dict-select v-model="form.state" dict_code="sys_user_state" placeholder="请选择状态" />
                </el-form-item>
                <el-form-item label="角色" prop="role_ids">
                    <el-select v-model="form.role_ids" value-key="id" multiple placeholder="请选择角色" clearable>
                        <el-option v-for="item in roles" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="所属组织" prop="organization_id">
                    <el-tree-select
                        v-model="form.organization_id"
                        :data="organization_tree"
                        node-key="id"
                        clearable
                        check-strictly
                        default-expand-all
                        :props="treeDefaultProps" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <el-button @click="handleCurrentDialogClose">取消</el-button>
            <el-button type="primary" @click="handleUserSave">确定</el-button>
        </template>
    </el-dialog>
</template>
