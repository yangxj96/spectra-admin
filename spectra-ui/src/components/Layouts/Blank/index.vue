<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { type RouteLocationMatched, useRouter } from "vue-router";

import Footer from "@/components/Layouts/components/Footer/index.vue";
import Navbar from "@/components/Layouts/components/Navbar/index.vue";

defineOptions({
    name: "LayoutsBlank"
});

const router = useRouter();

// 面包屑
const breadcrumb = ref<RouteLocationMatched[]>([]);

onMounted(() => {
    handlerRouter();

    watch(
        () => router.currentRoute.value.matched,
        value => {
            handlerRouter([...value]);
        },
        {
            immediate: true,
            deep: true
        }
    );
});

function handlerRouter(r: RouteLocationMatched[] = []) {
    if (r.length <= 0) {
        r = [...router.currentRoute.value.matched];
    }
    breadcrumb.value = r;
}
</script>

<template>
    <el-container class="box">
        <el-header class="box-header">
            <Navbar />
        </el-header>

        <el-container class="box-container">
            <el-main class="box-main">
                <!-- 内容部分 -->
                <div ref="content" class="box-content loading-box">
                    <router-view></router-view>
                </div>
                <!-- 底部版权 -->
                <Footer />
            </el-main>
        </el-container>
    </el-container>
</template>

<style scoped lang="scss">
.box {
    height: 100vh;
}

.box-aside {
    width: 64px;
}

.box-header {
    height: auto;
    border-bottom: solid 1px var(--el-border-color);
    padding: 0;
}

.box-container {
    height: calc(100vh - 66px);
}

.box-main {
    padding: 0;

    .box-breadcrumb {
        margin-top: 0.5vh;
        margin-bottom: 0.5vh;
        padding-left: 2vh;
        padding-right: 2vh;
        background-color: var(--el-bg-color);

        .form-item {
            margin-bottom: 0;
        }

        .form-item-end {
            margin-right: 0;
        }
    }

    .box-content {
        width: 100%;
        // 面包屑3.6vh,有个地方计算错了,改成5vh,头高62px,底部版权20px
        // 5vh - 面包屑
        // 62px - 头部固定高度
        // 20px - 底部版权固定高度
        height: calc(100vh - 62px - 20px);
        overflow: auto;
    }

    .box-content > div {
        padding: 10px;
    }

    .box-content:-webkit-full-screen {
        background-color: var(--el-bg-color);
    }

    .box-content:fullscreen {
        background-color: var(--el-bg-color);
    }

    .box-unfold-a {
        margin-right: 8px;
    }

    .footer {
        text-align: center;
        width: 100%;
        height: 20px;
        font-size: 10px;
        line-height: 20px;
        color: var(--el-text-color-regular);

        a {
            color: var(--el-text-color-primary);
        }
    }

    .box-unfold-a:hover {
        cursor: pointer;
    }
}
</style>
