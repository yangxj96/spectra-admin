<script setup lang="ts">
import DatabaseApi from "@/api/DatabaseApi.ts";
import { type ParsedMetrics, parseHikariCpMetrics } from "@/utils/HikariCpMetrics.ts";
import { ref } from "vue";

const m = ref<ParsedMetrics>();

try {
    const monitors = await DatabaseApi.getDatabaseMonitors();
    m.value = parseHikariCpMetrics(monitors.data);
} catch (e) {
    console.error(e);
}

</script>

<template>
    <!-- const url = import.meta.env.VITE_API_URL + "api/druid/index.html?t_" + Date.now(); -->
    <!-- <iframe :src="url" title="Druid监控页面" style="padding: 0; border: 0; height: 99%; width: 100%" /> -->
    <div>
        {{ m?.summary.healthStatus + "____" + m?.summary.message}}
        <p v-if="m" v-for="(item,idx) in m?.pools" :key="idx">
            {{ JSON.stringify(item)}}
        </p>
    </div>
</template>
