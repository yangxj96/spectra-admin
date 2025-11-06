<script setup lang="ts">
import { onMounted, useTemplateRef } from "vue";
import FcDesigner, { type Config } from "@form-create/designer";

const config = {
    switchType: false,
    showSaveBtn: true,
    showPreviewBtn: false,
    showDevice: false,
    showJsonPreview: false,
    showLanguage: false
} as Config;

const designer = useTemplateRef("designer");

function handleSave(data: { rule: string; options: string }) {
    console.log(`保存数据`);
    console.log(`路由规则: `, JSON.parse(data.rule));
    console.log(`配置规则: `, JSON.parse(data.options));
    let json = designer.value?.getJson();
    console.log(`json:${JSON.stringify(json)}`);
}

onMounted(() => {
    let json = designer.value?.getJson();
    console.log(`json:${JSON.stringify(json)}`);
});
</script>

<template>
    <el-row style="width: 100%; height: 85vh">
        <fc-designer ref="designer" class="designer" :config="config" @save="handleSave" />
    </el-row>
</template>

<style scoped lang="scss">
.designer {
    width: 100%;
    height: 100%;
}

:deep(.el-aside._fc-l-menu) {
    display: none !important;
}
</style>
