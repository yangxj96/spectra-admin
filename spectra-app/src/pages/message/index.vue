<script setup lang="ts">
import { useAuthGuard } from "@/hooks/useAuthGuard";
import useAppStore from "@/stores/app";
import type { MessageItem } from "@/types/index";
import { onMounted, ref } from "vue";

// #ifdef MP-WEIXIN
useAuthGuard();
// #endif

// 刷新状态
const refreshing = ref(false);
const appStore = useAppStore();
const notice = ref("");

onMounted(() => {
    notice.value = appStore.push_id
        ? `当前推送ID：${appStore.push_id}`
        : "未获取到推送ID，可能是获取失败或者尚未获取。";
});

// 消息列表
const list = ref<MessageItem[]>([
    {
        id: 1,
        title: "道一",
        note: "消息预览",
        badge: "11",
        avatar: "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/unicloudlogo.png"
    },
    {
        id: 2,
        title: "道二",
        note: "消息预览",
        badge: "12",
        avatar: "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/unicloudlogo.png"
    },
    {
        id: 3,
        title: "道三",
        note: "消息预览",
        badge: "13",
        avatar: "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/unicloudlogo.png"
    },
    {
        id: 4,
        title: "道四",
        note: "消息预览",
        badge: "14",
        avatar: "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/unicloudlogo.png"
    }
]);

// 触发刷新事件
const onListRefresh = async () => {
    refreshing.value = true;

    await new Promise(resolve => setTimeout(resolve, 1000));

    list.value = [
        {
            id: Date.now(),
            title: "新消息",
            note: "刷新出来的",
            badge: "1",
            avatar: "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/unicloudlogo.png"
        },
        ...list.value
    ] satisfies MessageItem[];

    refreshing.value = false;
};

// 列表点击事件
const handlerItemClick = (item: MessageItem) => {
    console.log("点击了消息项：", item);
    uni.navigateTo({
        url: `/subpackages/message/chat/index?id=${item.id}`
    });
};
</script>

<template>
    <view class="page">
        <!-- 头部固定 -->
        <uni-nav-bar statusBar fixed title="消息" />
        <!-- 公告 -->
        <uni-notice-bar single :text="notice" />
        <!-- 搜索） -->
        <uni-search-bar :focus="false" placeholder="请输入搜索内容" />
        <!-- 列表区域（可滚动 + 下拉刷新） -->
        <scroll-view
            class="list-scroll"
            scroll-y
            refresher-enabled
            :refresher-triggered="refreshing"
            @refresherrefresh="onListRefresh">
            <uni-list :border="true">
                <uni-list-chat
                    clickable
                    @click="handlerItemClick(item)"
                    v-for="item in list"
                    :key="item.id"
                    :title="item.title"
                    :note="item.note"
                    :badge-text="item.badge"
                    :avatar="item.avatar" />
            </uni-list>
        </scroll-view>
    </view>
</template>

<style scoped lang="scss">
.page {
    display: flex;
    flex-direction: column;
    height: 100vh;
}

/* 列表滚动区域 */
.list-scroll {
    flex: 1;
}
</style>
