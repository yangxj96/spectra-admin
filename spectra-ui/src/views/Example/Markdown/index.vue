<script setup lang="ts">
import { PDFViewer, type PDFViewerConfig } from "@embedpdf/vue-pdf-viewer";

const config = {
    src: "https://snippet.embedpdf.com/ebook.pdf",
    theme: { preference: "system" },
    i18n: {
        defaultLocale: "zh-CN",
        fallbackLocale: "en"
    }
} satisfies PDFViewerConfig;
</script>

<template>
    <div style="height: 100vh">
        <PDFViewer :config="config" :style="{ width: '100%', height: '100%' }" />
    </div>
</template>
