<template>
    <el-container class="box">
        <el-header class="box-header">
            <navbar />
        </el-header>

        <el-container class="box-container">
            <el-aside width="200px">
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
                    <el-col :span="3">
                        <el-form inline>
                            <el-form-item class="form-item">
                                <el-switch
                                    v-model="theme"
                                    active-text="深色模式"
                                    inactive-text="浅色模式"
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
                            <el-form-item class="form-item">
                                <icons
                                    name="icon-fullScreen"
                                    class="box-unfold-a"
                                    style="width: 1.4em; height: 1.4em"
                                    @click="toggle" />
                            </el-form-item>
                        </el-form>
                    </el-col>
                </el-row>
                <div ref="content" class="box-content loading-box">
                    <router-view></router-view>
                </div>
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

<script lang="ts" setup>
import Navbar from "@/components/Layout/components/navbar/index.vue";
import Sidebar from "@/components/Layout/components/sidebar/index.vue";
import { type RouteLocationMatched, useRouter } from "vue-router";
import { onMounted, ref, useTemplateRef, watch } from "vue";
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
function handleDarkSwitch(val: boolean) {
    theme.value = val;
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

<style scoped lang="scss">
::v-deep(.el-aside) {
    width: auto;
}

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

.form-item {
    margin-bottom: 0;
}

.box-main {
    padding: 0;

    .box-breadcrumb {
        margin-top: 1vh;
        padding-left: 2vh;
        padding-right: 2vh;
        background-color: var(--el-bg-color);
    }

    .box-content {
        width: 100%;
        // 面包屑3.6vh,有个地方计算错了,改成5vh,头高62px,底部版权20px
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
