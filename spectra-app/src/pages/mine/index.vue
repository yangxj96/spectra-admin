<script setup lang="ts">
import { DEFAULT_AVATAR } from "@/config/default";
import { useAuthGuard } from "@/hooks/useAuthGuard";
import { ref } from "vue";

// #ifdef MP-WEIXIN
useAuthGuard();
// #endif

const userAvatar = ref("/static/example/avatar.jpg");

// 头像加载失败回调,去除头像显示默认图
const handleAvatarError = () => {
    userAvatar.value = DEFAULT_AVATAR;
};
</script>

<template>
    <uni-nav-bar statusBar fixed title="" />

    <view class="header-box">
        <uni-row>
            <uni-col :span="8" style="height: 100%; display: flex; justify-content: center; align-items: center">
                <t-avatar :image="userAvatar" shape="round" size="200rpx" @error="handleAvatarError" />
            </uni-col>
            <uni-col :span="16" style="padding-left: 12rpx">
                <uni-row style="margin-top: 20rpx">
                    <text style="font-size: 40rpx">道一.</text>
                </uni-row>
                <uni-row style="margin-top: 20rpx">
                    <text style="font-size: 32rpx; color: #666">微信号: xxxxxxx</text>
                </uni-row>
                <uni-row style="margin-top: 20rpx">
                    <text style="font-size: 32rpx; color: #666">状态: 无</text>
                </uni-row>
            </uni-col>
        </uni-row>
    </view>

    <!-- #ifdef APP -->
    <scroll-view style="flex: 1">
        <!-- #endif -->
        <view class="features-group">
            <t-cell-group>
                <t-cell title="服务" hover arrow>
                    <template #left-icon>
                        <t-icon prefix="icons" name="service" color="#48B0EB" size="48rpx" />
                    </template>
                </t-cell>
            </t-cell-group>
        </view>

        <view class="features-group">
            <t-cell-group>
                <t-cell title="收藏" hover arrow>
                    <template #left-icon>
                        <t-icon prefix="icons" name="collect" color="#FED547" size="48rpx" />
                    </template>
                </t-cell>
            </t-cell-group>
        </view>

        <view class="features-group">
            <t-cell-group>
                <t-cell title="设置" hover arrow>
                    <template #left-icon>
                        <t-icon prefix="icons" name="settings" color="#38A65A" size="48rpx" />
                    </template>
                </t-cell>
            </t-cell-group>
        </view>

        <!-- #ifdef APP -->
    </scroll-view>
    <!-- #endif -->
</template>

<style lang="scss">
page {
    background-color: #f1f0f0;
}
</style>

<style lang="scss" scoped>
.header-box {
    height: 240rpx;
    background-color: white;
    padding-left: 20rpx;
    padding-right: 20rpx;
    padding-top: 20rpx;
}

// 去除导航栏底部边框
:deep(.uni-navbar--border) {
    border-bottom-style: none !important;
}

.features-group {
    margin-top: 20rpx;
    background-color: white;
}
</style>
