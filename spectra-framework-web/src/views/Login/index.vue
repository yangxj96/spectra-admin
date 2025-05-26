<template>
    <div class="box">
        <el-dialog
            :model-value="true"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :show-close="false"
            class="dialog-login"
            width="20%">
            <template #header>
                <p>
                    <icons name="icon-login" style="color: #9b9b9b" />
                    用户登录
                </p>
            </template>
            <div>
                <el-form ref="loginForm" label-width="60px" :model="user" :rules="rules">
                    <el-form-item label="账号" prop="username">
                        <el-input v-model="user.username" placeholder="请输入账号" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="user.password" placeholder="请输入密码" show-password />
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <el-button type="primary" @click="handleLogin(loginForm)">
                    <icons name="icon-login" />
                    <span>&nbsp;登录</span>
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import useUserStore from "@/plugin/store/modules/useUserStore";
import AuthApi from "@/api/AuthApi.ts";

const router = useRouter();
const loginForm = ref<FormInstance>();

const user = reactive<User>({
    username: "sysadmin",
    password: "sysadmin"
});

const rules = reactive<FormRules<User>>({
    username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }]
});

async function handleLogin(formElement: FormInstance | undefined) {
    // 没获取到表单对象
    if (!formElement) {
        return;
    }
    // 开始验证
    await formElement.validate((valid, fields) => {
        if (!valid) {
            ElMessage.error({
                message: "请检查表单"
            });
            console.log("错误字段为:", fields);
            return;
        }

        AuthApi.login(user.username, user.password).then(res => {
            if (res && res.code === 0 && res.data) {
                ElMessage.success({
                    duration: 500,
                    message: "登录成功",
                    onClose() {
                        useUserStore().token = res.data!;
                        router.push({ path: "/" });
                    }
                });
            }
        });
    });
}

interface User {
    username: string;
    password: string;
}
</script>

<style scoped lang="scss">
.box {
    height: 100vh;
    background-color: white;
}

:deep(.dialog-login) {
    left: 30%;
    top: 30vh;
}

:deep(.el-dialog__body) {
    padding-bottom: 0;
}

:deep(.el-dialog__footer) {
    padding-top: 0;
}
</style>
