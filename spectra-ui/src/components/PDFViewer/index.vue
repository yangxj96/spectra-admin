<script setup lang="ts">
import { createPluginRegistration } from "@embedpdf/core";
import { EmbedPDF } from "@embedpdf/core/vue";
import { usePdfiumEngine } from "@embedpdf/engines/vue";
import { DocumentContent, DocumentManagerPluginPackage } from "@embedpdf/plugin-document-manager/vue";
import { RenderLayer, RenderPluginPackage } from "@embedpdf/plugin-render/vue";
import { Scroller, ScrollPluginPackage } from "@embedpdf/plugin-scroll/vue";
import { Viewport, ViewportPluginPackage } from "@embedpdf/plugin-viewport/vue";

defineOptions({
    name: "PDFViewer"
});

// 1. 使用 Vue 组合式函数初始化引擎
const { engine, isLoading } = usePdfiumEngine();

// 2. 注册需要的插件
const plugins = [
    createPluginRegistration(DocumentManagerPluginPackage, {
        initialDocuments: [{ url: "https://snippet.embedpdf.com/ebook.pdf" }]
    }),
    createPluginRegistration(ViewportPluginPackage),
    createPluginRegistration(ScrollPluginPackage),
    createPluginRegistration(RenderPluginPackage)
];
</script>

<template>
    <div v-if="isLoading || !engine" class="loading-pane">加载PDF引擎中...</div>
    <div v-else style="height: calc(100% - 20px);)">
        <EmbedPDF :engine="engine" :plugins="plugins" v-slot="{ activeDocumentId }">
            <DocumentContent v-if="activeDocumentId" :document-id="activeDocumentId" v-slot="{ isLoaded }">
                <Viewport v-if="isLoaded" :document-id="activeDocumentId" style="background-color: #f1f3f5">
                    <Scroller :document-id="activeDocumentId">
                        <template #default="{ page }">
                            <div :style="{ width: page.width + 'px', height: page.height + 'px' }">
                                <RenderLayer :document-id="activeDocumentId" :page-index="page.pageIndex" />
                            </div>
                        </template>
                    </Scroller>
                </Viewport>
            </DocumentContent>
        </EmbedPDF>
    </div>
</template>

<style scoped lang="scss">
.loading-pane {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
}
</style>
