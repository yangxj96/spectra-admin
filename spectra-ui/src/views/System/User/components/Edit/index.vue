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
    default: {
        ...({} as User),
        timezone: "Asia/Shanghai",
        language: "zh-CN"
    }
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
    status: [{ required: true, message: "请选择状态", trigger: "blur" }],
    timezone: [{ required: true, message: "请选择时区", trigger: "blur" }],
    organization_id: [{ required: true, message: "请选择所属组织", trigger: "blur" }]
} as FormRules;

// 数据
const roles = ref<Role[]>();
const organization_tree = ref<OrganizationTree[]>();

// 组件
const formRef = useTemplateRef<FormInstance>("formRef");

onMounted(() => {
    if (!form.value.timezone) {
        form.value.timezone = "Asia/Shanghai";
    }

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

const EMAIL_SUFFIXES = ["devops00.com", "gmail.com", "qq.com", "hotmail.com"];

const handleEmailSuggestions = (query: string, cb: any) => {
    // 没有 @ 不提示
    if (!query || !query.includes("@")) {
        cb([]);
        return;
    }

    const [name, domainPart] = query.split("@");

    // @ 前为空也不提示
    if (!name) {
        cb([]);
        return;
    }

    const results = EMAIL_SUFFIXES.filter(suffix => suffix.startsWith(domainPart!)).map(suffix => ({
        value: `${name}@${suffix}`
    }));

    cb(results);
};
</script>

<template>
    <!-- 新增或编辑 -->
    <el-drawer v-model="open" :modal="true" modal-penetrable destroy-on-close @close="handleCurrentDialogClose">
        <template #header>
            <div>
                <icons name="icon-edit" />
                {{ (form.id ? "编辑" : "新增") + "用户" }}
            </div>
        </template>

        <template #default>
            <el-watermark style="height: 100%; width: 100%">
                <el-form ref="formRef" :model="form" :rules="rules" label-width="auto" @submit.prevent>
                    <el-form-item label="名称" prop="username">
                        <el-input v-model="form.username" placeholder="请输入名称" />
                    </el-form-item>
                    <el-form-item label="真实名称" prop="real_name">
                        <el-input v-model="form.real_name" placeholder="请输入真实名称" />
                    </el-form-item>
                    <el-form-item label="状态" prop="status">
                        <dict-select v-model="form.status" dict_code="sys_user_state" placeholder="请选择状态" />
                    </el-form-item>
                    <el-form-item label="性别" prop="gender">
                        <dict-select v-model="form.gender" dict_code="sys_user_gender" placeholder="请选择性别" />
                    </el-form-item>
                    <el-form-item label="生日" prop="birthday">
                        <el-date-picker
                            v-model="form.birthday"
                            type="date"
                            placeholder="请输入选择"
                            value-format="YYYY-MM-DD"
                            style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="手机号码" prop="phone">
                        <el-input v-model="form.phone" placeholder="请输入手机号码" />
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                        <el-autocomplete
                            v-model="form.email"
                            :fetch-suggestions="handleEmailSuggestions"
                            placeholder="请输入邮箱">
                            <template #suffix>
                                <el-tooltip effect="dark" content="同时也作为默认登录账号" placement="right">
                                    <icons name="icon-hint" style="margin-left: 10px; width: 1.4em; height: 1.4em" />
                                </el-tooltip>
                            </template>
                        </el-autocomplete>
                    </el-form-item>
                    <el-form-item label="国家" prop="country">
                        <el-input v-model="form.country" placeholder="请输入国家" />
                    </el-form-item>
                    <el-form-item label="城市" prop="city">
                        <el-input v-model="form.city" placeholder="请输入城市" />
                    </el-form-item>
                    <el-form-item label="语言" prop="language">
                        <dict-select v-model="form.language" dict_code="sys_language" placeholder="请选择语言" />
                    </el-form-item>
                    <el-form-item label="时区" prop="timezone">
                        <dict-select v-model="form.timezone" dict_code="sys_timezone" placeholder="请选择时区" />
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
            </el-watermark>
        </template>

        <template #footer>
            <el-button @click="handleCurrentDialogClose">取消</el-button>
            <el-button type="primary" @click="handleUserSave">确定</el-button>
        </template>
    </el-drawer>
</template>
