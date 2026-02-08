<script setup lang="ts">
import { computed, onMounted, type PropType, ref, watch } from "vue";
import { useDictStore } from "@/plugin/store/modules/use-dict-store.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

defineOptions({
    name: "DictSelect"
});

const model = defineModel<string | number | null>({
    required: true,
    default: null
});

const dict_code = defineModel("dict_code", {
    required: true,
    type: String as PropType<string>
});

const dictStore = useDictStore();

const options = ref<DictItem[]>([]);

// localValue 用来绑定 el-select，支持字符串/数字/空
const localValue = ref<string | number | undefined>(undefined);

// 计算绑定，保证双向绑定
const localComputed = computed({
    get() {
        return localValue.value ?? undefined;
    },
    set(val: string | number) {
        localValue.value = val ?? undefined;
        model.value = localValue.value;
    }
});

// 挂载的时候读取字典
onMounted(async () => {
    try {
        options.value = (await dictStore.getDictData(dict_code.value)) || [];

        // 如果外部 model 未传值，使用 default_flag 的值
        if (model.value === undefined) {
            const defaultItem = options.value.find(item => item.default_flag);
            if (defaultItem) {
                localValue.value = defaultItem.value;
                model.value = defaultItem.value;
            }
        } else {
            // 如果外部传入了 model，则覆盖 default_flag
            localValue.value = model.value ?? undefined;
        }
    } catch {
        MessageUtils.error("获取字典数据失败");
    }
});

// 监听外部 model 改变，同步 localValue
watch(model, val => {
    if (val !== localValue.value) {
        localValue.value = val ?? undefined;
    }
});
</script>

<template>
    <el-select v-model="localComputed" v-bind="{ clearable: true, 'append-to': '.box-content', ...$attrs }">
        <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.value" />
    </el-select>
</template>
