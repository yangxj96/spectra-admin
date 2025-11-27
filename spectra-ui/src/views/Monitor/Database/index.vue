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
    <div>
        {{ m?.summary.healthStatus + "____" + m?.summary.message}}
        <p v-if="m" v-for="(item,idx) in m?.pools" :key="idx">
            {{ JSON.stringify(item)}}
        </p>
    </div>
</template>
