<script setup lang="ts"></script>

<template>
    <uni-nav-bar showMenuButtonWidth statusBar fixed title="" />
    <uni-row style="height: 240rpx; background-color: white; padding-left: 20rpx; padding-right: 20rpx">
        <uni-col :span="8" style="height: 100%; display: flex; justify-content: center; align-items: center">
            <image src="/src/static/default/avatar.jpg" mode="scaleToFill" style="height: 200rpx; width: 200rpx" />
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

    <!-- #ifdef APP -->
    <scroll-view style="flex: 1">
        <!-- #endif -->
        <uni-list style="margin-top: 20rpx; background-color: white">
            <uni-list-item
                style="height: 80rpx"
                title="服务"
                showBadge
                badgeText="20"
                link
                to="/subpackages/message/chat/index" />
        </uni-list>
        <uni-list style="margin-top: 20rpx; background-color: white">
            <uni-list-item title="收藏" link to="/subpackages/message/chat/indexx" />
            <uni-list-item title="朋友圈" link to="/subpackages/message/chat/index" />
            <uni-list-item title="作品" link to="/subpackages/message/chat/index" />
            <uni-list-item title="卡包" link to="/subpackages/message/chat/index" />
            <uni-list-item title="表情" link to="/subpackages/message/chat/index" />
        </uni-list>
        <uni-list style="margin-top: 20rpx; background-color: white">
            <uni-list-item title="设置" link to="/subpackages/message/chat/index" />
        </uni-list>
        <!-- #ifdef APP -->
    </scroll-view>
    <!-- #endif -->
</template>

<style lang="scss">
page {
    background-color: #f1f0f0;
}
</style>

<style lang="scss" scoped></style>
