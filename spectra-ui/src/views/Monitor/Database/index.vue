<script setup lang="ts">
const url = import.meta.env.VITE_API_URL + "api/druid/index.html?t_" + Date.now();
// const url = "https://localhost:4004/api/druid/index.html?t_" + Date.now();
</script>

<template>
    <iframe :src="url" title="Druid监控页面" style="padding: 0; border: 0; height: 99%; width: 100%" />
</template>
