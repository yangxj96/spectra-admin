<script setup lang="ts">
import { DEFAULT_FEATURE } from "@/config/default";
import { ref } from "vue";

const back = () => {
    uni.navigateBack();
};

const notice = ref("这是一个公告，展示在消息列表页面的顶部，可以用于显示重要信息或者通知。");
</script>

<template>
    <view class="layout">
        <uni-nav-bar showMenuButtonWidth statusBar fixed title="我是一个标题" left-icon="left" @clickLeft="back" />

        <view class="body">
            <uni-notice-bar single :text="notice" />
            <!-- #ifdef APP -->
            <scroll-view class="scroll">
                <!-- #endif -->
                <uni-section title="常用" type="line" v-for="(item, index) in 5" :index="index" :key="index">
                    <t-grid
                        class="t-grid badge"
                        :hover="true"
                        theme="card"
                        v-for="(item, index) in 2"
                        :index="index"
                        :key="index">
                        <t-grid-item
                            @click="
                                () => {
                                    console.log('点击了功能项', index);
                                }
                            "
                            t-class-image="image"
                            :text="'标题文字' + index"
                            :image="DEFAULT_FEATURE"
                            :badge-props="{ count: 8 }"
                            v-for="(item, index) in 4"
                            :index="index"
                            :key="index" />
                    </t-grid>
                </uni-section>

                <!-- #ifdef APP -->
            </scroll-view>
            <!-- #endif -->
        </view>
    </view>
</template>

<style scoped>
.layout {
    height: 100vh;
    display: flex;
    flex-direction: column;
    padding-top: var(--window-top);
}

.body {
    flex: 1;
}

.scroll {
    height: 100%;
}
</style>
