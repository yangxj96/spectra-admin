<script setup lang="ts">
import { useDark, useFullscreen, useToggle } from "@vueuse/core";
import { onMounted, ref, useTemplateRef, watch } from "vue";
import { type RouteLocationMatched, useRouter } from "vue-router";

import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import Footer from "@/components/Layouts/components/Footer/index.vue";
import Navbar from "@/components/Layouts/components/Navbar/index.vue";
import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";

import Sidebar from "./components/sidebar/index.vue";

defineOptions({
    name: "LayoutsDefault"
});

const appStore = useAppStore();
const router = useRouter();
const context = useTemplateRef<HTMLElement>("content");

// 面包屑
const breadcrumb = ref<RouteLocationMatched[]>([]);
const { toggle } = useFullscreen(context);
const fullscreenToggle = toggle;
const theme = ref(useDark());

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

// 深色模式切换
function handleDarkSwitch(val: unknown) {
    theme.value = val as boolean;
    useToggle(theme);
}

function handlerRouter(r: RouteLocationMatched[] = []) {
    const current = router.currentRoute.value;
    // ① 优先使用 meta.crumbs
    const crumbs = current.meta?.crumbs;
    if (Array.isArray(crumbs) && crumbs.length > 0) {
        breadcrumb.value = crumbs.map(c => ({
            path: c.path ?? current.path,
            meta: { title: c.title }
        })) as RouteLocationMatched[];
        return;
    }

    // ② fallback：使用路由 matched
    if (r.length <= 0) {
        r = [...router.currentRoute.value.matched];
    }
    breadcrumb.value = r;
}

function handleMenu() {
    appStore.unfold = !appStore.unfold;
}
</script>

<template>
    <el-container class="box">
        <el-header class="box-header">
            <Navbar />
        </el-header>

        <el-container class="box-container">
            <el-aside width="10vw">
                <Sidebar />
            </el-aside>

            <el-main class="box-main">
                <!-- 面包屑横条 -->
                <el-row class="box-breadcrumb">
                    <el-col :span="21">
                        <i class="box-unfold-a" @click="handleMenu">
                            <ComponentsIcons v-if="appStore.unfold" name="icon-fold-left" />
                            <ComponentsIcons v-else name="icon-fold-right" />
                        </i>
                        <!-- 面包屑 -->
                        <el-breadcrumb style="display: inline-block">
                            <el-breadcrumb-item v-for="(item, idx) in breadcrumb" :key="idx" :to="{ path: item.path }">
                                {{ item.meta.title }}
                            </el-breadcrumb-item>
                        </el-breadcrumb>
                    </el-col>
                    <!-- 右边工具条 -->
                    <el-col :span="3">
                        <el-form inline style="float: right">
                            <!-- 深色浅色模式切换 -->
                            <el-form-item class="form-item">
                                <el-switch
                                    v-model="theme"
                                    active-text="浅色"
                                    inactive-text="深色"
                                    inline-prompt
                                    :size="'small'"
                                    @change="handleDarkSwitch">
                                    <template #active-action>
                                        <ComponentsIcons name="icon-moon" />
                                    </template>
                                    <template #inactive-action>
                                        <ComponentsIcons name="icon-sun" />
                                    </template>
                                </el-switch>
                            </el-form-item>
                            <!-- 全屏切换 -->
                            <el-form-item class="form-item form-item-end">
                                <ComponentsIcons
                                    name="icon-fullScreen"
                                    class="box-unfold-a"
                                    style="width: 1.4em; height: 1.4em"
                                    @click="fullscreenToggle" />
                            </el-form-item>
                        </el-form>
                    </el-col>
                </el-row>
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
        height: calc(100vh - 5vh - 62px - 20px);
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
