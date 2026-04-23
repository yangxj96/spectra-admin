<script setup lang="ts">
import { type AutocompleteData, type FormInstance, type FormRules } from "element-plus";
import { onMounted, ref, useTemplateRef } from "vue";

import { roleApi } from "@/api/auth/role.ts";
import { departmentApi } from "@/api/user/organization.ts";
import { userApi } from "@/api/user/user.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import DictSelect from "@/components/DictSelect/index.vue";
import { userConverter } from "@/converter/user-converter.ts";
import { useDictStore } from "@/plugin/store/modules/use-dict-store.ts";
import { treeDefaultProps } from "@/utils/default-config.ts";
import { MessageUtils } from "@/utils/message-utils.ts";
import { email, mobile } from "@/utils/verify-rules.ts";

// 定义Model
const form = defineModel<UserForm>("form", {
    required: true
});

const open = defineModel<boolean>("open", { required: true, default: false });

// 定义响应方法
const emits = defineEmits(["close"]);

// 表单规则
const rules: FormRules<UserForm> = {
    username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    email: [
        { required: true, message: "请输入邮箱", trigger: "blur" },
        { validator: email, trigger: "blur" }
    ],
    phone: [{ validator: mobile, trigger: "blur" }],
    status: [{ required: true, message: "请选择状态", trigger: "blur" }],
    timezone: [{ required: true, message: "请选择时区", trigger: "blur" }],
    department_id: [{ required: true, message: "请选择所属组织", trigger: "blur" }]
};

// 数据
const roles = ref<RolePageVO[]>();
const department_tree = ref<DepartmentTreeVO[]>();

// 组件
const formRef = useTemplateRef<FormInstance>("formRef");

// 缓存 suffix 列表
const emailSuffixes = ref<string[]>([]);

onMounted(async () => {
    useDictStore()
        .getDictData("sys_email_suffix")
        .then(res => {
            const items = res || [];
            emailSuffixes.value = items.map(i => i.value);
        });

    roles.value = await roleApi.list();
    department_tree.value = await departmentApi.tree();
});

// 处理关闭
const handleClose = () => {
    open.value = false;
    emits("close");
};

// 新增或编辑用户
const handleUserSave = async () => {
    if (!formRef.value) return;
    try {
        await formRef.value?.validate();
        if (form.value.id) {
            await userApi.update(userConverter.toDTO(form.value));
        } else {
            await userApi.create(userConverter.toDTO(form.value));
        }
        MessageUtils.success(form.value.id ? "修改用户成功" : "新增用户成功", handleClose);
    } catch (error) {
        console.error(error);
        MessageUtils.error(error);
    }
};

// 处理自动提示补全组件的补全过程
const handleEmailSuggestions = (query: string, callback: (results: AutocompleteData) => void) => {
    if (!query) {
        callback([]);
        return;
    }

    let name = "";
    let domainPart = "";

    if (query.includes("@")) {
        [name = "", domainPart = ""] = query.split("@");
    } else {
        name = query;
    }

    if (!name) {
        callback([]);
        return;
    }

    const results = emailSuffixes.value
        .filter(
            suffix =>
                // 没输入 @ 或 @ 后为空 → 全部展示
                !domainPart || suffix.includes(domainPart)
        )
        .map(suffix => ({
            value: `${name}@${suffix}`
        }));

    callback(results);
};
</script>

<template>
    <!-- 新增或编辑 -->
    <el-drawer v-model="open" class="loading-box" :modal="true" @close="handleClose">
        <template #header>
            <div>
                <ComponentsIcons name="icon-edit" />
                {{ (form.id ? "编辑" : "新增") + "用户" }}
            </div>
        </template>

        <template #default>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="auto" @submit.prevent>
                <el-form-item label="名称" prop="username">
                    <el-input v-model="form.username" clearable placeholder="请输入名称" />
                </el-form-item>
                <el-form-item label="真实名称" prop="real_name">
                    <el-input v-model="form.real_name" clearable placeholder="请输入真实名称" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <DictSelect v-model="form.status" dict_code="sys_user_state" placeholder="请选择状态" />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                    <DictSelect v-model="form.gender" dict_code="sys_user_gender" placeholder="请选择性别" />
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
                    <el-input v-model="form.phone" clearable placeholder="请输入手机号码" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-autocomplete
                        v-model="form.email"
                        :fetch-suggestions="handleEmailSuggestions"
                        clearable
                        placeholder="请输入邮箱">
                        <template #suffix>
                            <el-tooltip effect="dark" content="同时也作为默认登录账号" placement="right">
                                <ComponentsIcons
                                    name="icon-hint"
                                    style="margin-left: 10px; width: 1.4em; height: 1.4em" />
                            </el-tooltip>
                        </template>
                    </el-autocomplete>
                </el-form-item>
                <el-form-item label="国家" prop="country">
                    <el-input v-model="form.country" clearable placeholder="请输入国家" />
                </el-form-item>
                <el-form-item label="城市" prop="city">
                    <el-input v-model="form.city" clearable placeholder="请输入城市" />
                </el-form-item>
                <el-form-item label="语言" prop="language">
                    <DictSelect v-model="form.language" dict_code="sys_language" placeholder="请选择语言" />
                </el-form-item>
                <el-form-item label="时区" prop="timezone">
                    <DictSelect v-model="form.timezone" dict_code="sys_timezone" placeholder="请选择时区" />
                </el-form-item>
                <el-form-item label="角色" prop="role_ids">
                    <el-select v-model="form.role_ids" value-key="id" multiple placeholder="请选择角色" clearable>
                        <el-option v-for="item in roles" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="所属组织" prop="department_id">
                    <el-tree-select
                        v-model="form.department_id"
                        :data="department_tree"
                        node-key="id"
                        clearable
                        check-strictly
                        default-expand-all
                        :props="treeDefaultProps" />
                </el-form-item>
                <el-form-item label="用户数据范围">
                    <el-select v-model="form.data_scope" placeholder="请选择用户数据范围" style="width: 100%">
                        <el-option label="全局" :value="0" />
                        <el-option label="本人" :value="1" />
                        <el-option label="部门" :value="2" />
                        <el-option label="部门及子部门" :value="3" />
                        <el-option label="自定义" :value="4" />
                    </el-select>
                </el-form-item>
                <el-form-item v-if="form.data_scope === 4" label="自定义数据范围">
                    <el-tree-select
                        v-model="form.target_ids"
                        :data="department_tree"
                        node-key="id"
                        clearable
                        check-strictly
                        multiple
                        default-expand-all
                        :props="treeDefaultProps" />
                </el-form-item>
            </el-form>
        </template>

        <template #footer>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="handleUserSave">确定</el-button>
        </template>
    </el-drawer>
</template>
