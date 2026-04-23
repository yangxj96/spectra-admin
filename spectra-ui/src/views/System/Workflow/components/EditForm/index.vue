<script setup lang="ts">
import { type Config } from "@form-create/designer";
import { defineAsyncComponent, onMounted, useTemplateRef } from "vue";

const FcDesigner = defineAsyncComponent({
    loader: () => import("@form-create/designer"),
    delay: 200,
    timeout: 30000
});

const config = {
    switchType: false,
    showSaveBtn: true,
    showPreviewBtn: false,
    showDevice: false,
    showJsonPreview: false,
    showLanguage: false
} as Config;

const designer = useTemplateRef<InstanceType<typeof FcDesigner>>("designer");

const handleSave = (data: { rule: string; options: string }) => {
    console.log(`保存数据`);
    console.log(`路由规则: `, JSON.parse(data.rule));
    console.log(`配置规则: `, JSON.parse(data.options));
    const json = designer.value?.getJson();
    console.log(`json:${JSON.stringify(json)}`);
};

onMounted(() => {
    const json = designer.value?.getJson();
    console.log(`json:${JSON.stringify(json)}`);
});
</script>

<template>
    <el-row style="width: 100%; height: 85vh">
        <FcDesigner ref="designer" class="designer" :config="config" @save="handleSave" />
    </el-row>
</template>

<style scoped lang="scss">
.designer {
    width: 100%;
    height: 100%;
}

:deep(.el-aside ._fc-l-menu) {
    display: none !important;
}
</style>
