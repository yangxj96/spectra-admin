<script setup lang="ts">
import FileUpload from "@/components/FileUplaod/index.vue";
import { ref } from "vue";

const fileUploadShow = ref(false);
</script>

<template>
    <el-button @click="() => (fileUploadShow = true)">打开文件上传</el-button>
    <file-upload v-model="fileUploadShow" />
</template>
