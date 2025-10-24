<script setup lang="ts">
import BpmnModeler from "bpmn-js/lib/Modeler";

// DOM 容器引用
const containerRef = ref<HTMLElement | undefined>(undefined);

// Modeler 实例
let modeler: BpmnModeler;

onMounted(() => {
    // 确保 DOM 已挂载
    if (!containerRef.value) return;

    // 创建 Modeler 实例
    modeler = new BpmnModeler({
        container: containerRef.value
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
            (modeler.get("canvas") as object).zoom("fit-viewport");
        })
        .catch((error: Error) => {
            console.error("Failed to import BPMN diagram", error);
        });
});
</script>

<template>
    <el-row style="width: 100%">
        <el-col :span="20">
            <div ref="containerRef" class="bpmn-container"></div>
        </el-col>
        <el-col :span="4"></el-col>
    </el-row>
</template>

<style lang="css" src="bpmn-js/dist/assets/diagram-js.css"></style>
<style lang="css" src="bpmn-js/dist/assets/bpmn-font/css/bpmn.css"></style>
<style lang="css" src="bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css"></style>
<style lang="css" src="bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css"></style>
<style lang="css" src="@bpmn-io/properties-panel/dist/assets/properties-panel.css"></style>
<style lang="scss" scoped>
.bpmn-container {
    width: 100%;
    height: 84vh;
    border: 1px solid #ccc;
    overflow: hidden;
}
</style>
