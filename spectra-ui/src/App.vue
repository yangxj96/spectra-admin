<script setup lang="ts">
import { reactive, ref } from "vue";

import { useAppStore } from "@/plugin/store/modules/use-app-store.ts";

import type { Language } from "element-plus/es/locale";

const locale = ref(useAppStore().lang as Language);
const message = reactive({
    max: 3,
    duration: 500,
    plain: true,
    appendTo: ".box-content"
});
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
