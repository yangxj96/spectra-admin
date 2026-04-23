<script setup lang="ts">
import { useDark } from "@vueuse/core";
import { ElForm, type FormRules } from "element-plus";
import { computed, reactive, ref, useTemplateRef } from "vue";
import { useRoute, useRouter } from "vue-router";

import { authApi } from "@/api/auth/auth.ts";
import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";
import { MessageUtils } from "@/utils/message-utils.ts";
import { loginParticlesDark, loginParticlesLight } from "@/views/Login/config/login-particles.ts";

const isDark = useDark();

const particlesOptions = computed(() => (isDark.value ? loginParticlesDark : loginParticlesLight));
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
    } as LoginForm,
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
const refreshKaptcha = () => {
    kaptchaUrl.value = import.meta.env.VITE_API_URL + "api/common/kaptcha?_t=" + Date.now();
};

// 登录
const handleLogin = async () => {
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
        const token = await authApi.login(login.form);
        MessageUtils.success("登录成功", () => {
            useUserStore().token = token;
            useUserStore().isLoggedIn = true;
            const path = "/redirect" + (redirect.value ?? "");
            router.push({ path });
        });
    } catch (error) {
        // 登录失败，刷新验证码
        refreshKaptcha();
        console.error("登录请求失败:", error);
    }
};
</script>

<template>
    <div class="box">
        <vue-particles id="particles" :options="particlesOptions" />

        <el-dialog
            :model-value="true"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :show-close="false"
            class="dialog-login"
            width="20%">
            <template #header>
                <p>
                    <ComponentsIcons name="icon-login" style="color: #9b9b9b" />
                    用户登录
                </p>
            </template>
            <div>
                <ElForm ref="loginForm" label-width="70px" :model="login.form" :rules="login.rules">
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
                                        <div class="el-image__error" style="">
                                            <ComponentsIcons name="icon-loading" class-name="v-code-ico" />
                                            加载中...
                                        </div>
                                    </template>
                                </el-image>
                            </el-col>
                        </el-row>
                    </el-form-item>
                </ElForm>
            </div>
            <template #footer>
                <el-button type="primary" @click="handleLogin">
                    <ComponentsIcons name="icon-login" />
                    <span>&nbsp;登录</span>
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped lang="scss">
.box {
    height: 100vh;
    background:
        radial-gradient(circle at 20% 80%, var(--el-color-page-background) 0%, transparent 40%),
        radial-gradient(circle at 80% 20%, var(--el-color-page-background) 0%, transparent 40%),
        radial-gradient(circle at 50% 50%, #020617 0%, #000000 100%);
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
    margin: 4px;
    //border-radius: 10px;
    cursor: pointer;
}

.v-code-ico {
    color: var(--el-text-color-placeholder);
    animation: v-code-ico-rotate 2s linear infinite;
}

@keyframes v-code-ico-rotate {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}

.v-code:hover {
    opacity: 0.8;
    transform: scale(1.02);
    transition: all 0.2s ease;
}

#particles {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
}
</style>
