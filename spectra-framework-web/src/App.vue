<template>
    <div id="nav">
        <el-config-provider :locale="locale" :message="message">
            <router-view />
        </el-config-provider>
    </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import useAppStore from "@/plugin/store/modules/useAppStore";
import type { MessageConfigContext } from "element-plus";

const locale = ref(useAppStore().lang);
const message = reactive<MessageConfigContext>({
    max: 3,
    duration: 500
});
</script>

<style lang="scss">
@use "@/assets/css/common.scss";

* {
    padding: 0;
    margin: 0;
}
</style>
