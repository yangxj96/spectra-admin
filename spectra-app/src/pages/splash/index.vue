<template>
    <!-- #ifdef APP -->
    <scroll-view style="flex: 1">
        <!-- #endif -->

        <view class="container">
            <view class="splash">
                <image class="logo" src="/static/logo.png" />
                <text class="title">光谱App</text>
            </view>
        </view>

        <!-- #ifdef APP -->
    </scroll-view>
    <!-- #endif -->
</template>

<script setup lang="ts">
import { bootstrap } from "@/helper/bootstrap";
import useAppStore from "@/stores/app";
import { onMounted } from "vue";

const appStore = useAppStore();

onMounted(async () => {
    await bootstrap();
    // 控制最小展示时间（避免闪屏）
    await new Promise(resolve => setTimeout(resolve, 1000));
    next();
});

function next() {
    if (appStore.isFirstLaunch) {
        uni.reLaunch({ url: "/pages/message/index" });
        return;
    }

    if (!appStore.token) {
        uni.reLaunch({ url: "/pages/login/login" });
        return;
    }

    uni.reLaunch({ url: "/pages/message/index" });
}
</script>

<style lang="scss" scoped>
.container {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #ffffff;
}

.splash {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.logo {
    width: 140px;
    height: 140px;
}

.title {
    margin-top: 20px;
    font-size: 22px;
    font-weight: bold;
}
</style>
