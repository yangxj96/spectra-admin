<script setup lang="ts">
import { reactive, ref, useTemplateRef } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElForm, type FormRules } from "element-plus";
import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";
import { authApi } from "@/api/auth/auth.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

const route = useRoute();
const router = useRouter();
const loginRef = useTemplateRef<InstanceType<typeof ElForm>>("loginForm");
const kaptchaUrl = ref(import.meta.env.VITE_API_URL + "api/common/kaptcha?_t=" + Date.now());
const redirect = ref<string>(route.query.redirect as string | "/");
const login = reactive({
    form: {
        type: "PASSWORD",
        username: "",
        password: "",
        captcha: ""
    } as LoginFrom,
    rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }]
    } as FormRules
});

// 开发环境添加个账号名密码,省的输入
if (import.meta.env.DEV) {
    login.form.username = "devops@devops00.com";
    login.form.password = "admin123";
    login.form.captcha = "1";
}

// 刷新验证码
function refreshKaptcha() {
    kaptchaUrl.value = import.meta.env.VITE_API_URL + "api/common/kaptcha?_t=" + Date.now();
}

// 登录
async function handleLogin() {
    // 没获取到表单对象
    if (!loginRef) {
        return;
    }

    // 开始验证
    const valid = await loginRef.value?.validate();
    if (!valid) {
        MessageUtils.error("请检查表单");
        console.log("验证未通过");
        return;
    }

    try {
        const res = await authApi.login(login.form);
        if (res && res.code === 200 && res.data) {
            MessageUtils.success("登录成功", () => {
                useUserStore().token = res.data!;
                useUserStore().isLoggedIn = true;
                const path = "/redirect" + (redirect.value ?? "");
                router.push({ path });
            });
        }
    } catch (error) {
        // 登录失败，刷新验证码
        refreshKaptcha();
        console.error("登录请求失败:", error);
    }
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
                <el-form ref="loginForm" label-width="70px" :model="login.form" :rules="login.rules">
                    <el-form-item label="账号" prop="username">
                        <el-input v-model="login.form.username" placeholder="请输入账号" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="login.form.password" placeholder="请输入密码" show-password />
                    </el-form-item>
                    <el-form-item label="验证码" prop="captcha">
                        <el-row style="width: 100%">
                            <el-col :span="12">
                                <el-input v-model="login.form.captcha" placeholder="请输入验证码" />
                            </el-col>
                            <el-col :span="12">
                                <el-image :src="kaptchaUrl" class="v-code" @click="refreshKaptcha">
                                    <template v-slot:placeholder>
                                        {{ "验证码加载中..." }}
                                    </template>
                                </el-image>
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
    cursor: pointer;
}

.v-code:hover {
    opacity: 0.8;
    transform: scale(1.02);
    transition: all 0.2s ease;
}
</style>
