<template>
    <iframe title="swagger" :src="src"></iframe>
</template>
<script setup lang="ts">
import { ref } from "vue";

const src = ref("http://localhost:8888/swagger-ui/index.html");
</script>

<style scoped lang="scss">
iframe {
    width: calc(100% - 32px);
    height: calc(100% - 32px);
}
</style>
