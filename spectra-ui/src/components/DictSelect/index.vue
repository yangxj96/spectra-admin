<script setup lang="ts">
import { computed, onMounted, type PropType, ref } from "vue";
import UseDictStore from "@/plugin/store/modules/useDictStore";
import MessageHelp from "@/utils/MessageHelper.ts";

const model = defineModel({
    type: [String, Number, null] as PropType<string | number | null | undefined>,
    required: true,
    default: undefined
});

const dict_code = defineModel("dict_code", {
    required: true,
    type: String as PropType<string>
});

const dictStore = UseDictStore();

const options = ref<DictData[]>([]);

const localValue = computed({
    get() {
        return model.value === undefined ? "" : String(model.value);
    },
    set(val: string) {
        model.value = val === "" ? undefined : Number.isNaN(Number(val)) ? val : Number(val);
    }
});

// 挂载的时候读取字典
onMounted(async () => {
    try {
        options.value = (await dictStore.getDictData(dict_code.value)) || [];
    } catch {
        MessageHelp.error("获取字典数据失败");
    }
});
</script>

<template>
    <el-select v-model="localValue" v-bind="{ clearable: true, 'append-to': '.box-content', ...$attrs }">
        <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.value" />
    </el-select>
</template>
