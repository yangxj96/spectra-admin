<script setup lang="ts">
import { ref, useTemplateRef } from "vue";
import "echarts";
import VChart from "vue-echarts";

const charts = useTemplateRef<HTMLDivElement>("charts");

const grid = {
    left: 80,
    right: 50
};
const width = 1000 - grid.left - grid.right;
const data = [];
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
    <v-chart ref="charts" :option="option" autoresize class="chart" />
</template>

<style lang="scss" scoped>
.chart {
    width: 100%;
    height: 100%;
}
</style>
