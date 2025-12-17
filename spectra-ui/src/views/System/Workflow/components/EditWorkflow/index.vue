<script setup lang="ts">
import LogicFlow from "@logicflow/core";
import "@logicflow/core/dist/index.css";
import { onMounted, useTemplateRef } from "vue";
import FlowablePlugin, * as Flowable from "@yangxj96/logicflow-flowable";
import { Control, DndPanel, SelectionSelect } from "@logicflow/extension";
import "@logicflow/extension/dist/index.css";

const container = useTemplateRef<HTMLDivElement>("container");

const panel = useTemplateRef<HTMLDivElement>("panel");

let logicFlow: LogicFlow;

onMounted(() => {
    if (!container.value) return;

    logicFlow = new LogicFlow({
        container: container.value!,
        grid: true,
        plugins: [Control, DndPanel, SelectionSelect, FlowablePlugin],
        pluginsOptions: {
            selectionSelect: {
                exclusiveMode: false
            }
        }
    });

    logicFlow.getGraphData();

    (logicFlow.extension.dndPanel as DndPanel)?.setPatternItems(Flowable.getFlowableDndItems());
    (logicFlow.extension.control as Control)?.addItem({
        key: "export",
        title: "",
        text: "导出",
        iconClass: "export",
        onClick: lf => {
            console.log(lf);
            let xml = Flowable.toBpmnXml(lf);
            console.log(xml);
        }
    });

    logicFlow.render({});

    Flowable.registerPropertyPanel({
        container: panel.value!,
        lf: logicFlow
    });
});
</script>

<template>
    <el-row style="height: 100%">
        <el-col :span="18" style="height: 100%">
            <div ref="container" style="height: 100%; width: 100%" />
        </el-col>
        <el-col :span="6" style="height: 100%">
            <div ref="panel" style="height: 100%; width: 100%" />
        </el-col>
    </el-row>
</template>
