<script setup lang="ts">
import Local from "./components/Local/index.vue";

const type = ref("本地存储");
const num = ref(1001);
</script>
<template>
    <div>文件存储,在考虑怎么存储</div>
    <div>1</div>
    <local :type="type" :num="num" />
</template>
