<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";

const redirect = ref("");

const form = ref({
    username: "",
    password: "",
    captcha: ""
});

// 模拟验证码（实际项目换成后端接口）
const captchaUrl = ref(getCaptcha());

onLoad((options: any) => {
    if (options.redirect) {
        // 关键：解码两次
        redirect.value = decodeURIComponent(options.redirect);
        console.log("redirect:", redirect.value);
    }
});

function getCaptcha() {
    return `https://dummyimage.com/120x40/007aff/fff&text=${Math.random().toString(36).slice(2, 6)}`;
}

/**
 * 刷新验证码
 */
function refreshCaptcha() {
    captchaUrl.value = getCaptcha();
}

// 登录逻辑
function handleLogin() {
    if (!form.value.username) {
        uni.showToast({ title: "请输入用户名", icon: "none" });
        return;
    }
    if (!form.value.password) {
        uni.showToast({ title: "请输入密码", icon: "none" });
        return;
    }
    if (!form.value.captcha) {
        uni.showToast({ title: "请输入验证码", icon: "none" });
        return;
    }

    // TODO: 调用接口
    console.log("登录参数", form.value);

    uni.showLoading({ title: "登录中..." });

    setTimeout(() => {
        uni.hideLoading();
        uni.showToast({ title: "登录成功" });
    }, 1000);
}
</script>

<template>
    <!-- #ifdef APP -->
    <scroll-view style="flex: 1">
        <!-- #endif -->

        <view class="page">
            <uni-nav-bar statusBar fixed title="登录" />

            <view class="container">
                <!-- 标题 -->
                <view class="title">欢迎登录</view>

                <!-- 用户名 -->
                <uni-easyinput v-model="form.username" placeholder="请输入用户名" prefixIcon="person" />

                <!-- 密码 -->
                <uni-easyinput v-model="form.password" type="password" placeholder="请输入密码" prefixIcon="locked" />

                <!-- 验证码 -->
                <view class="captcha-box">
                    <uni-easyinput v-model="form.captcha" placeholder="验证码" />
                    <image :src="captchaUrl" class="captcha-img" @click="refreshCaptcha" />
                </view>

                <!-- 登录按钮 -->
                <button class="login-btn" @click="handleLogin">登录</button>
            </view>
        </view>

        <!-- #ifdef APP -->
    </scroll-view>
    <!-- #endif -->
</template>

<style lang="scss" scoped>
.page {
    min-height: 100vh;
    background: linear-gradient(180deg, #007aff 0%, #ffffff 300px);
}

.container {
    padding: 40rpx;
    margin-top: 100rpx;
}

.title {
    font-size: 48rpx;
    font-weight: bold;
    margin-bottom: 60rpx;
    text-align: center;
    color: #333;
}

.captcha-box {
    display: flex;
    gap: 20rpx;
    margin-top: 20rpx;
}

.captcha-img {
    width: 200rpx;
    height: 80rpx;
    border-radius: 10rpx;
}

.login-btn {
    margin-top: 60rpx;
    background-color: #007aff;
    color: #fff;
    border-radius: 50rpx;
}
</style>
