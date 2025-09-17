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
    <el-select v-model="localValue" :placeholder="placeholder" clearable append-to=".box-content">
        <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.value" />
    </el-select>
</template>
