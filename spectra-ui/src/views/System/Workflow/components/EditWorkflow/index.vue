<script setup lang="ts">
import LogicFlow from "@logicflow/core";
import "@logicflow/core/dist/index.css";
import "@logicflow/extension/dist/index.css";
import "@yangxj96/logicflow-flowable/dist/index.css";
import { Control, SelectionSelect } from "@logicflow/extension";
import Flowable, { type FlowablePluginOptions } from "@yangxj96/logicflow-flowable";
import { onMounted, useTemplateRef } from "vue";

const container = useTemplateRef<HTMLDivElement>("container");
const graph = useTemplateRef<HTMLDivElement>("graph");
const panel = useTemplateRef<HTMLDivElement>("panel");

let logicFlow: LogicFlow;

onMounted(() => {
    if (!container.value) return;

    logicFlow = new LogicFlow({
        container: container.value!,
        grid: true,
        plugins: [Control, SelectionSelect, Flowable.Plugin],
        pluginsOptions: {
            selectionSelect: {
                exclusiveMode: false
            },
            flowable: {
                propertyPanel: {
                    enabled: true,
                    container: panel.value!,
                    defaultRenderers: true
                },
                dndPanel: {
                    enabled: true,
                    container: graph.value!
                }
            } as FlowablePluginOptions
        }
    });

    (logicFlow.extension.control as Control)?.addItem({
        key: "export",
        title: "",
        text: "导出",
        iconClass: "export",
        onClick: lf => {
            console.log(lf);
            const xml = Flowable.toBpmnXml(lf);
            console.log(xml);
        }
    });

    logicFlow.render({});
});
</script>

<template>
    <el-row style="height: 100%">
        <el-col :span="4" class="col">
            <div ref="graph" style="height: 100%; width: 100%" />
        </el-col>
        <el-col :span="14" style="height: 100%">
            <div ref="container" style="height: 100%; width: 100%" />
        </el-col>
        <el-col :span="6" style="height: 100%">
            <div ref="panel" style="height: 100%; width: 100%" />
        </el-col>
    </el-row>
</template>
