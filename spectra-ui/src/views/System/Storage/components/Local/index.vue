<script setup lang="ts">
const type = defineModel<string>("type");
const num = defineModel<number>("num");
</script>

<template>
    <div>存储方式:{{ type }},数量:{{ num }}</div>
</template>

<style scoped lang="scss"></style>
