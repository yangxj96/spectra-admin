<script setup lang="ts">
import { LineChart } from "echarts/charts";
import { GridComponent, LegendComponent, TitleComponent, TooltipComponent } from "echarts/components";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { defineAsyncComponent, onMounted, ref, useTemplateRef } from "vue";

import { serviceMonitorApi } from "@/api/system/service-monitor.ts";

const VChart = defineAsyncComponent(() => import("vue-echarts"));
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, TitleComponent, LegendComponent]);

// 相关信息
const cpuInfo = ref<CPUInfo>();
const ramInfo = ref<RAMInfo>();
const jvmInfo = ref<JVMInfo>();

// echarts配置
const chartCPUOption = {
    tooltip: {
        trigger: "axis"
    },
    xAxis: {
        type: "category",
        data: [],
        boundaryGap: false
    },
    yAxis: {
        type: "value",
        min: 0,
        max: 100,
        name: "使用率(%)"
    },
    series: [
        {
            name: "CPU使用率",
            type: "line",
            smooth: true,
            data: [],
            areaStyle: {
                color: {
                    type: "linear",
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [
                        {
                            offset: 0,
                            color: "rgba(58, 162, 235, 0.5)"
                        },
                        {
                            offset: 1,
                            color: "rgba(58, 162, 235, 0.1)"
                        }
                    ]
                }
            },
            itemStyle: {
                color: "#3a82eb"
            }
        }
    ]
};

// ref内容
const cpuChart = useTemplateRef("cpuChart");

onMounted(() => {
    initData();
    // 初始更新
    updateData();
    // 定时更新数据（2秒一次）
    setInterval(updateData, 2000);
});

// 初始化所需数据
const initData = () => {
    const requests = [serviceMonitorApi.getCPUInfo(), serviceMonitorApi.getRAMInfo(), serviceMonitorApi.getJVMInfo()];
    Promise.all(requests).then(res => {
        cpuInfo.value = (res[0] ?? []) as CPUInfo;
        ramInfo.value = (res[1] ?? []) as RAMInfo;
        jvmInfo.value = (res[2] ?? []) as JVMInfo;
    });
};

//////////////// 以下位辅助方法,还需要整理

// 时间数据
const timeData = [] as string[];
const cpuData = [] as number[];
let currentTime = new Date();

const updateData = () => {
    // 生成随机CPU使用率
    const cpuValue = Math.floor(Math.random() * 45) + 20; // 20-65%范围

    // 更新时间和数据数组
    currentTime = new Date();
    const timeStr =
        currentTime.getHours() +
        ":" +
        (currentTime.getMinutes() < 10 ? "0" + currentTime.getMinutes() : currentTime.getMinutes()) +
        ":" +
        (currentTime.getSeconds() < 10 ? "0" + currentTime.getSeconds() : currentTime.getSeconds());

    timeData.push(timeStr);
    cpuData.push(cpuValue);

    // 保持数据长度为20
    if (timeData.length > 20) {
        timeData.shift();
        cpuData.shift();
    }

    // 更新折线图
    cpuChart.value?.setOption({
        xAxis: {
            data: timeData
        },
        series: [
            {
                data: cpuData
            }
        ]
    });
};
</script>

<template>
    <div>
        <!-- 固定内容行 -->
        <el-row style="margin-top: 1vh">
            <el-col :span="8">
                <el-card class="card" body-class="card-body" header="CPU信息">
                    <el-row>CPU型号: {{ cpuInfo?.name }}</el-row>
                    <el-row>完整标识: {{ cpuInfo?.identifier }}</el-row>
                    <el-row>
                        <el-col :span="8">家族编号: {{ cpuInfo?.family }}</el-col>
                        <el-col :span="8">型号编号: {{ cpuInfo?.model }}</el-col>
                        <el-col :span="8">修订版本: {{ cpuInfo?.stepping }}</el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12">制造商: {{ cpuInfo?.vendor }}</el-col>
                        <el-col :span="12">是否64位: {{ cpuInfo?.is64bit ? "是" : "否" }}</el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12">物理核心数: {{ cpuInfo?.physical_cores }}</el-col>
                        <el-col :span="12">逻辑核心数: {{ cpuInfo?.logical_cores }}</el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12">最大频率: {{ cpuInfo?.max_frequency_hz }}</el-col>
                        <el-col :span="12">最大频率: {{ cpuInfo?.max_frequency_ghz }}</el-col>
                    </el-row>
                </el-card>
            </el-col>

            <el-col :span="8">
                <el-card class="card" body-class="card-body" header="内存信息">
                    <el-row>
                        <el-col :span="12">合计:{{ ramInfo?.summary }}</el-col>
                    </el-row>
                    <el-row>
                        <el-col :span="12">数量合计:{{ ramInfo?.count }}</el-col>
                        <el-col :span="12">容量合计:{{ ramInfo?.total_capacity_gb }}</el-col>
                    </el-row>
                    <el-tabs model-value="1">
                        <el-tab-pane
                            v-for="(item, idx) in ramInfo?.slots"
                            :key="idx"
                            :index="idx"
                            :name="item.slot + ''"
                            :label="'内存' + item.slot + '(' + item.memory_type + ')'">
                            <el-row>
                                <el-col :span="12">频率:{{ item?.clock_speed_mhz }}</el-col>
                                <el-col :span="12">容量:{{ item?.capacity_gb }}</el-col>
                            </el-row>
                        </el-tab-pane>
                    </el-tabs>
                </el-card>
            </el-col>

            <el-col :span="8">
                <el-card class="card" body-class="card-body-jvm" header="JVM信息">
                    <el-row>名称: {{ jvmInfo?.jvm_name }}</el-row>
                    <el-row>版本: {{ jvmInfo?.jvm_version }}</el-row>
                    <el-row>
                        <el-col :span="12">供应商: {{ jvmInfo?.jvm_vendor }}</el-col>
                        <el-col :span="12">主页: {{ jvmInfo?.java_vendor_url }}</el-col>
                    </el-row>
                    <el-row>安装位置: {{ jvmInfo?.java_home }}</el-row>
                    <el-row>
                        <el-col :span="12">PID:{{ jvmInfo?.process_id }}</el-col>
                        <el-col :span="12">启动时间:{{ jvmInfo?.start_time }}</el-col>
                    </el-row>
                </el-card>
            </el-col>
        </el-row>

        <!-- 系统负载和CPU -->
        <el-row style="margin-top: 1vh">
            <el-col :span="12">
                <el-card class="card" header="系统负载[折线图]"></el-card>
            </el-col>
            <el-col :span="12">
                <el-card class="card" header="CPU使用率">
                    <VChart ref="cpuChart" :option="chartCPUOption" autoresize style="width: 100%; height: 24vh" />
                </el-card>
            </el-col>
        </el-row>

        <!-- 内存使用率和JVM堆内存 -->
        <el-row style="margin-top: 1vh">
            <el-col :span="12">
                <el-card class="card" header="内存使用率[仪表盘或面积图]"></el-card>
            </el-col>
            <el-col :span="12">
                <el-card class="card" header="JVM堆内存[双轴折线图]"></el-card>
            </el-col>
        </el-row>

        <!-- 线程数和GC次数 -->
        <el-row style="margin-top: 1vh">
            <el-col :span="12">
                <el-card class="card" header="线程数[折线图]"></el-card>
            </el-col>
            <el-col :span="12">
                <el-card class="card" header="GC次数[柱状图]"></el-card>
            </el-col>
        </el-row>
    </div>
</template>

<style scoped lang="scss">
.card {
    width: 98%;
    height: 30vh;
}

.card-body > div {
    line-height: 1.6rem;
}

.card-body-jvm > div {
    line-height: 2rem;
}
</style>
