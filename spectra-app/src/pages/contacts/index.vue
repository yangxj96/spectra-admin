<script setup lang="ts">
import { ref } from "vue";
import { useAuthGuard } from "@/hooks/useAuthGuard";

// #ifdef MP-WEIXIN
useAuthGuard();
// #endif

const list = ref([
    {
        index: "A",
        children: ["阿坝", "阿拉善", "阿里", "安康", "安庆", "鞍山", "安顺", "安阳", "澳门"]
    },
    {
        index: "B",
        children: [
            "北京",
            "白银",
            "保定",
            "宝鸡",
            "保山",
            "包头",
            "巴中",
            "北海",
            "蚌埠",
            "本溪",
            "毕节",
            "滨州",
            "百色",
            "亳州"
        ]
    },
    {
        index: "C",
        children: [
            "重庆",
            "成都",
            "长沙",
            "长春",
            "沧州",
            "常德",
            "昌都",
            "长治",
            "常州",
            "巢湖",
            "潮州",
            "承德",
            "郴州",
            "赤峰",
            "池州",
            "崇左",
            "楚雄",
            "滁州",
            "朝阳"
        ]
    },
    {
        index: "D",
        children: [
            "大连",
            "东莞",
            "大理",
            "丹东",
            "大庆",
            "大同",
            "大兴安岭",
            "德宏",
            "德阳",
            "德州",
            "定西",
            "迪庆",
            "东营"
        ]
    },
    {
        index: "E",
        children: ["鄂尔多斯", "恩施", "鄂州"]
    },
    {
        index: "F",
        children: ["福州", "防城港", "佛山", "抚顺", "抚州", "阜新", "阜阳"]
    },
    {
        index: "G",
        children: ["广州", "桂林", "贵阳", "甘南", "赣州", "甘孜", "广安", "广元", "贵港", "果洛"]
    },
    {
        index: "J",
        children: ["揭阳", "吉林", "晋江", "吉安", "胶州", "嘉兴", "济南", "鸡西", "荆州", "江门", "基隆"]
    },
    {
        index: "K",
        children: ["昆明", "开封", "康定", "喀什"]
    }
]);
</script>

<template>
    <!-- 头部保留 -->
    <uni-nav-bar statusBar fixed title="通讯录" />

    <view class="content-box">
        <!-- 搜索栏 -->
        <uni-search-bar placeholder="请输入搜索内容" />

        <!-- 联系人列表 -->
        <t-indexes :sticky="false" :sticky-offset="120">
            <block v-for="(item, index) in list" :key="index">
                <t-indexes-anchor :index="item.index" />
                <t-cell-group>
                    <t-cell v-for="(city, index1) in item.children" :key="index1" :title="city" aria-role="button" />
                </t-cell-group>
            </block>
        </t-indexes>
    </view>
</template>

<style scoped>
.page {
    display: flex;
    flex-direction: column;
    height: 100vh;
}

.content-box {
    /* 核心代码：*/
    /* --window-top 是 uni-app 提供的 CSS 变量，*/
    /* 它会自动等于「状态栏高度 + 原生导航栏高度」。*/
    /* 这样就能完美避开 fixed 导航栏的遮挡，且兼容 H5、App 和小程序。*/
    padding-top: var(--window-top);
    flex: 1;
}
</style>
