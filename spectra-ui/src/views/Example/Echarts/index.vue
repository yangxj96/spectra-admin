<script setup lang="ts">
import { ScatterChart } from "echarts/charts";
import { TitleComponent, GridComponent, TooltipComponent } from "echarts/components";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { ref, defineAsyncComponent } from "vue";

/* 动态加载 vue-echarts 组件 */
const VChart = defineAsyncComponent(() => import("vue-echarts"));

/* 注册需要的组件 */
use([ScatterChart, TitleComponent, GridComponent, TooltipComponent, CanvasRenderer]);

const grid = {
    left: 80,
    right: 50
};

const width = 1000 - grid.left - grid.right;

const data: unknown[] = [];

for (let day = 0; day < 7; ++day) {
    for (let i = 0; i < 1000; ++i) {
        const y = Math.tan(i) / 2 + 7;
        data.push([day, y, Math.random()]);
    }
}

const option = ref({
    title: {
        text: "带有抖动的分散排列"
    },
    grid,
    xAxis: {
        type: "category",
        jitter: (width / 7) * 0.8,
        data: ["周一", "周二", "周三", "周四", "周五", "周六", "周日"]
    },
    yAxis: {
        type: "value",
        max: 10,
        min: 0
    },
    series: [
        {
            name: "Sleeping Hours",
            type: "scatter",
            data,
            colorBy: "data",
            itemStyle: {
                opacity: 0.4
            }
        }
    ]
});
</script>

<template>
    <VChart :option="option" autoresize class="chart" />
</template>

<style scoped lang="scss">
.chart {
    width: 100%;
    height: 100%;
}
</style>
