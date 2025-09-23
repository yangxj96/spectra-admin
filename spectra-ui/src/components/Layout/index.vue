<script setup lang="ts">
import { type RouteLocationMatched } from "vue-router";
import useAppStore from "@/plugin/store/modules/useAppStore.ts";
import { useDark, useFullscreen, useToggle } from "@vueuse/core";

const appStore = useAppStore();
const router = useRouter();
const context = useTemplateRef<HTMLElement>("content");

// 面包屑
const breadcrumb = ref<RouteLocationMatched[]>([]);
const { toggle } = useFullscreen(context);
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
            <navbar />
        </el-header>

        <el-container class="box-container">
            <el-aside width="10vw">
                <sidebar />
            </el-aside>

            <el-main class="box-main">
                <!-- 面包屑横条 -->
                <el-row class="box-breadcrumb">
                    <el-col :span="21">
                        <i class="box-unfold-a" @click="handleMenu">
                            <icons v-if="appStore.unfold" name="icon-fold-left" />
                            <icons v-else name="icon-fold-right" />
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
                                        <icons name="icon-moon" />
                                    </template>
                                    <template #inactive-action>
                                        <icons name="icon-sun" />
                                    </template>
                                </el-switch>
                            </el-form-item>
                            <!-- 全屏切换 -->
                            <el-form-item class="form-item form-item-end">
                                <icons
                                    name="icon-fullScreen"
                                    class="box-unfold-a"
                                    style="width: 1.4em; height: 1.4em"
                                    @click="toggle" />
                            </el-form-item>
                        </el-form>
                    </el-col>
                </el-row>
                <!-- 内容部分 -->
                <div ref="content" class="box-content loading-box">
                    <router-view></router-view>
                </div>
                <!-- 底部版权 -->
                <el-footer class="footer">
                    Copyright © 2018-2023
                    <strong><a href="//yangxj96.com/" target="_blank">yangxj96</a></strong>
                    &nbsp;
                    <strong><a href="//yangxj96.com/" target="_blank">yangxj96.com</a></strong>
                    All Rights Reserved. 备案号：
                    <a target="_blank" rel="nofollow" href="https://beian.miit.gov.cn/">滇ICP备2023006063号-1</a>
                </el-footer>
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
