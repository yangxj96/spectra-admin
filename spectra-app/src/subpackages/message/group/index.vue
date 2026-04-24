<script lang="ts" setup>
const back = () => {
    uni.navigateBack();
};
</script>

<template>
    <view class="page">
        <!-- 头部固定 -->
        <uni-nav-bar @clickLeft="back" showMenuButtonWidth statusBar left-icon="left" fixed title="群聊" />
    </view>
</template>
