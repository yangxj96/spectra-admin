<script setup lang="ts">
import AuthApi from "@/api/AuthApi.ts";
import useUserStore from "@/plugin/store/modules/useUserStore";
import { ElForm, ElMessage, type FormRules } from "element-plus";

const router = useRouter();
const loginRef = useTemplateRef<InstanceType<typeof ElForm>>("loginForm");
const kaptchaUrl = ref(import.meta.env.VITE_API_URL + "api/common/kaptcha?t=" + Date.now());

// 表单
const user = reactive<LongParams>({
    username: "yangxj96@gmail.com",
    password: "sysadmin",
    code: "1234"
});

// 路由
const rules = reactive<FormRules<LongParams>>({
    username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }],
    code: [{ required: true, message: "请输入验证码", trigger: "blur" }]
});

// 登录
async function handleLogin() {
    // 没获取到表单对象
    if (!loginRef) {
        return;
    }
    // 开始验证
    await loginRef.value?.validate((valid, fields) => {
        if (!valid) {
            ElMessage.error({
                message: "请检查表单"
            });
            console.log("错误字段为:", fields);
            return;
        }

        AuthApi.login(user.username, user.password, user.code).then(res => {
            if (res && res.code === 200 && res.data) {
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

// 登录参数
interface LongParams {
    username: string;
    password: string;
    code: string;
}
</script>

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
                <el-form ref="loginForm" label-width="70px" :model="user" :rules="rules">
                    <el-form-item label="账号" prop="username">
                        <el-input v-model="user.username" placeholder="请输入账号" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="user.password" placeholder="请输入密码" show-password />
                    </el-form-item>
                    <el-form-item label="验证码" prop="code">
                        <el-row style="width: 100%">
                            <el-col :span="12">
                                <el-input v-model="user.code" placeholder="请输入验证码" />
                            </el-col>
                            <el-col :span="12">
                                <el-image :src="kaptchaUrl" class="v-code" />
                            </el-col>
                        </el-row>
                    </el-form-item>
                </el-form>
            </div>
            <template #footer>
                <el-button type="primary" @click="handleLogin">
                    <icons name="icon-login" />
                    <span>&nbsp;登录</span>
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

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

.v-code {
    height: calc(var(--el-input-height, 32px) - 2px);
    width: 100%;
    padding: 4px;
    border-radius: 10px;
}
</style>
