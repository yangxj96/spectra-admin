<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const time = ref(-1);
const second = ref(3);

onMounted(() => {
    time.value = globalThis.setInterval(() => {
        second.value = second.value - 1;
        if (second.value <= 0) {
            router.replace("/");
        }
    }, 1000);
});

onUnmounted(() => {
    clearTimeout(time.value);
});

const handleBack = () => {
    if (time.value !== -1) {
        clearTimeout(time.value);
    }
    router.replace("/");
};
</script>

<template>
    <el-result icon="error" title="404" :sub-title="`无权访问该页面,${second}秒后自动跳转到首页`">
        <template #extra>
            <el-button type="primary" @click="handleBack">回首页</el-button>
        </template>
    </el-result>
</template>
