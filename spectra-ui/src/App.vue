<script setup lang="ts">
import useAppStore from "@/plugin/store/modules/useAppStore";
import type { Language } from "element-plus/es/locale";
import useUserStore from "@/plugin/store/modules/useUserStore.ts";
import AuthApi from "@/api/AuthApi.ts";
import { ElMessage } from "element-plus";
import GlobalUtils from "@/utils/GlobalUtils.ts";

const locale = ref(useAppStore().lang as Language);
const message = reactive({
    max: 3,
    duration: 500,
    plain: true
});

const userStore = useUserStore();

// 检查token
function check() {
    if (!userStore.token || userStore.token.access_token === undefined) {
        console.debug("无token信息");
        return;
    }
    AuthApi.check().then(res => {
        console.log(res);
        if (res.code !== 200) {
            ElMessage.error({
                message: res.msg,
                onClose() {
                    GlobalUtils.exit();
                }
            });
        }
    });
}

// 挂在的时候执行,这里也是整个APP进行挂载
onMounted(() => {
    check();
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
