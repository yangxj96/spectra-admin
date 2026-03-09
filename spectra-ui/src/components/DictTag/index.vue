<script setup lang="ts">
import { computed, onMounted, type PropType, ref, watch } from "vue";

import { useDictStore } from "@/plugin/store/modules/use-dict-store.ts";

defineOptions({
    name: "DictTag"
});

const model = defineModel({
    required: true,
    type: [String, Number] as PropType<string | number>
});

/**
 * 主键值,如果没设置的所有渲染的tag都是 type="primary"
 * 否则,设置的值为primary,其余的都会是danger
 */
const primary_value = defineModel("primary_value", {
    required: false,
    type: [String, Number] as PropType<string | number>
});

const dict_code = defineModel("dict_code", {
    required: true,
    type: [String] as PropType<string>
});

const dictStore = useDictStore();

const dict_data = ref<DictItem>();

// 封装一个方法来加载并匹配字典数据
const loadDictData = async () => {
    if (!dict_code.value) {
        dict_data.value = {} as DictItem;
        return;
    }

    const dictData = (await dictStore.getDictData(dict_code.value)) || [];

    if (dictData.length <= 0) {
        dict_data.value = {} as DictItem;
        return;
    }

    for (const item of dictData) {
        if (item.value === localValue.value) {
            dict_data.value = item;
            return;
        }
    }

    // 如果没找到匹配项，清空 dict_data
    dict_data.value = undefined;
};

// 本地值,使用实时计算的,获取的值为v-model的值
const localValue = computed({
    get() {
        return String(model.value) || "";
    },
    set(val) {
        model.value = Number.isNaN(Number(val)) ? val : Number(val);
    }
});

// 计算显示的 label 或 "未知"
const display_label = computed(() => {
    if (dict_data.value && dict_data.value.label) {
        return dict_data.value.label;
    }
    return "未知";
});

// 计算tagType
const tag_type = computed(() => {
    if (!primary_value.value) {
        return "primary";
    }
    if (dict_data.value) {
        return dict_data.value.value === String(primary_value.value) ? "primary" : "danger";
    }
    return "warning";
});

// 初次挂载时加载数据
onMounted(async () => {
    await loadDictData();
});

// 监听 props.dict_code 和 props.modelValue 变化
watch(
    () => [dict_code, model],
    async () => {
        await loadDictData();
    },
    { deep: true }
);
</script>

<template>
    <div>
        <el-tag v-if="dict_data" :type="tag_type">
            {{ display_label }}
        </el-tag>
    </div>
</template>
