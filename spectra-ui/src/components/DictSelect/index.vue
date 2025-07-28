<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import UseDictStore from "@/plugin/store/modules/useDictStore";

const props = defineProps({
    modelValue: {
        type: [String, Number] as PropType<string | number | undefined>,
        required: false,
        default: undefined
    },
    placeholder: {
        type: String as PropType<string>,
        default: ""
    },
    dict_code: {
        type: String as PropType<string>,
        required: true
    }
});

const emit = defineEmits(["update:modelValue"]);

const dictStore = UseDictStore();

const options = ref<DictData[]>([]);

const localValue = computed({
    get() {
        return props.modelValue === undefined ? "" : String(props.modelValue);
    },
    set(val) {
        const parsedVal = Number.isNaN(Number(val)) ? val : Number(val);
        emit("update:modelValue", parsedVal);
    }
});

// 挂载的时候读取字典
onMounted(async () => {
    try {
        options.value = (await dictStore.getDictData(props.dict_code)) || [];
    } catch {
        ElMessage.error("获取字典数据失败");
    }
});
</script>

<template>
    <el-select v-model="localValue" :placeholder="placeholder" clearable>
        <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.value" />
    </el-select>
</template>
