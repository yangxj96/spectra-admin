<script setup lang="ts">
import type { Language } from "element-plus/es/locale";
import { reactive, ref, watch } from "vue";
import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";
import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";
import { useAuthIdle } from "@/hooks/use-authIdle.ts";

const { start, stop } = useAuthIdle({
    idleTime: 10 * 60 * 1000 // 10分钟
});
const userStore = useUserStore();
const locale = ref(useAppStore().lang as Language);
const message = reactive({
    max: 3,
    duration: 500,
    plain: true,
    appendTo: ".box-content"
});

// 登录状态监听
watch(
    () => userStore.isLoggedIn,
    loggedIn => {
        if (loggedIn) {
            start();
        } else {
            stop();
        }
    },
    { immediate: true }
);
</script>

<template>
    <div id="nav">
        <el-config-provider :locale="locale" :message="message">
            <router-view />
        </el-config-provider>
    </div>
</template>

<style lang="scss">
@use "@/assets/css/common.scss";

* {
    padding: 0;
    margin: 0;
}
</style>
