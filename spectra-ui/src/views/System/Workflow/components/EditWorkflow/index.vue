<script setup lang="ts">
import LogicFlow from "@logicflow/core";
import "@logicflow/core/dist/index.css";
import { onMounted, useTemplateRef } from "vue";
import FlowablePlugin, * as Flowable from "@yangxj96/logicflow-flowable";
import { Control, DndPanel, SelectionSelect } from "@logicflow/extension";
import "@logicflow/extension/dist/index.css";

const container = useTemplateRef<HTMLDivElement>("container");

onMounted(() => {
    if (!container.value) return;

    const lf = new LogicFlow({
        container: container.value!,
        grid: true,
        plugins: [Control, DndPanel, SelectionSelect, FlowablePlugin],
        pluginsOptions: {
            selectionSelect: {
                exclusiveMode: false
            }
        }
    });

    lf.getGraphData();

    (lf.extension.dndPanel as DndPanel)?.setPatternItems(Flowable.getFlowableDndItems());

    lf.render({
        nodes: [
            {
                id: "n1",
                type: "bpmn:startEvent",
                x: 210,
                y: 110
            },
            {
                id: "n2",
                type: "bpmn:userTask",
                x: 410,
                y: 110
            },
            {
                id: "n3",
                type: "bpmn:endEvent",
                x: 610,
                y: 110
            }
        ],
        edges: [
            {
                id: "sf1",
                type: "bpmn:sequenceFlow",
                sourceNodeId: "n1",
                targetNodeId: "n2"
            },
            {
                id: "sf2",
                type: "bpmn:sequenceFlow",
                sourceNodeId: "n2",
                targetNodeId: "n3"
            }
        ]
    });

    console.log(Flowable.toBpmnXml(lf.getGraphData()));
});
</script>

<template>
    <el-row style="height: 100%">
        <div ref="container" style="height: 100%; width: 100%" />
    </el-row>
</template>
