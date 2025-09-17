<!--
  -  Copyright 2018-2025 yangxj96
  -
  -  Licensed under the Apache License, Version 2.0 (the "License");
  -  you may not use this file except in compliance with the License.
  -  You may obtain a copy of the License at
  -
  -      http://www.apache.org/licenses/LICENSE-2.0
  -
  -  Unless required by applicable law or agreed to in writing, software
  -  distributed under the License is distributed on an "AS IS" BASIS,
  -  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  -  See the License for the specific language governing permissions and
  -  limitations under the License.
  -->

<script setup lang="ts">
import "echarts";
import VChart from "vue-echarts";

const option = ref({
    xAxis: {
        type: "category",
        data: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
    },
    yAxis: {
        type: "value"
    },
    series: [
        {
            data: [150, 230, 224, 218, 135, 147, 260],
            type: "line"
        }
    ]
});
</script>

<template>
    <v-chart :option="option" autoresize class="chart" />
</template>

<style lang="scss" scoped>
.chart {
    width: 100%;
    height: 100%;
}
</style>
