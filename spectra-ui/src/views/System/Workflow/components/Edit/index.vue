<script setup lang="ts">
import { useTemplateRef } from "vue";
// 核心
import BpmnModeler from "bpmn-js/lib/Modeler";
import type { Canvas } from "bpmn-js/lib/features/context-pad/ContextPadProvider";
// i18n
import Translate from "@/views/System/Workflow/components/Edit/Translate.ts";
// 属性面板
import {
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    ZeebePropertiesProviderModule
} from "bpmn-js-properties-panel";
import ZeebeModdle from "zeebe-bpmn-moddle/resources/zeebe.json";
import ZeebeBehaviorsModule from "camunda-bpmn-js-behaviors/lib/camunda-cloud";

// DOM 容器引用
const containerRef = useTemplateRef<HTMLElement>("containerRef");
const propertiesPanelRef = useTemplateRef<HTMLElement>("propertiesPanelRef");

// Modeler 实例
let modeler: BpmnModeler;

const handleExportXML = async () => {
    try {
        const { xml } = await modeler.saveXML({ format: true });

        // 创建 Blob
        const blob = new Blob([xml!], { type: "application/xml;charset=utf-8" });

        // 创建下载链接
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "diagram.bpmn"; // 文件名
        document.body.append(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch (error) {
        console.error("Failed to export BPMN diagram", error);
    }
};

const handleExportSVG = async () => {
    try {
        const { svg } = await modeler.saveSVG();

        // 创建 Blob
        const blob = new Blob([svg!], { type: "application/xml;charset=utf-8" });

        // 创建下载链接
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "diagram.svg"; // 文件名
        document.body.append(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch (error) {
        console.error("Failed to export BPMN diagram", error);
    }
};

onMounted(() => {
    // 确保 DOM 已挂载
    if (!containerRef.value) return;

    // 创建 Modeler 实例
    modeler = new BpmnModeler({
        container: containerRef.value,
        propertiesPanel: {
            parent: propertiesPanelRef.value
        },
        additionalModules: [
            BpmnPropertiesPanelModule,
            BpmnPropertiesProviderModule,
            ZeebePropertiesProviderModule,
            ZeebeBehaviorsModule,
            Translate
        ],
        moddleExtensions: {
            zeebe: ZeebeModdle
        }
    });

    // 可选：导入一个空流程图
    const initialBpmnXML = `
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd"
             id="Definitions_1"
             targetNamespace="http://bpmn.io/schema/bpmn">
  <process id="Process_1" isExecutable="false">
    <startEvent id="StartEvent_1" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="102" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>
  `.trim();

    // 导入 XML
    modeler
        .importXML(initialBpmnXML)
        .then(() => {
            // 自适应视图
            modeler.get<Canvas>("canvas").zoom("fit-viewport");

            // Step 3: 初始化 Properties Panel（仅在挂载后）
            if (!propertiesPanelRef.value) return;
        })
        .catch((error: Error) => {
            console.error("Failed to import BPMN diagram", error);
        });
});
</script>

<template>
    <el-row style="padding: 10px">
        <el-button-group>
            <el-button type="primary" link @click="handleExportXML">保存</el-button>
            <el-button type="primary" link @click="handleExportXML">导出XML</el-button>
            <el-button type="primary" link @click="handleExportXML">导出XML</el-button>
            <el-button type="primary" link @click="handleExportSVG">导出SVG</el-button>
        </el-button-group>
    </el-row>
    <el-row style="width: 100%; padding-left: 10px; padding-right: 10px">
        <el-col :span="20">
            <div ref="containerRef" class="bpmn-container"></div>
        </el-col>
        <el-col :span="4" class="properties-panel-col">
            <div ref="propertiesPanelRef" class="properties-panel-container" style="height: 100%; overflow: auto"></div>
        </el-col>
    </el-row>
</template>

<style lang="css" src="bpmn-js/dist/assets/diagram-js.css"></style>
<style lang="css" src="bpmn-js/dist/assets/bpmn-font/css/bpmn.css"></style>
<style lang="css" src="bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css"></style>
<style lang="css" src="bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css"></style>
<style lang="css" src="@/views/System/Workflow/components/Edit/properties-panel.css"></style>
<style lang="scss" scoped>
.bpmn-container {
    width: 100%;
    height: 80vh;
    border: 1px solid #ccc;
    overflow: hidden;
}

.properties-panel-col {
    height: 100%;
    display: flex;
    flex-direction: column;
    padding-left: 10px;
    padding-right: 10px;
}

.properties-panel-container {
    height: 80vh !important;
}
</style>
