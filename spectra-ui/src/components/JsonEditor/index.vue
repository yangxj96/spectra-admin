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
import { onMounted, useTemplateRef, watch } from "vue";
import JSONEditor from "jsoneditor";
import "jsoneditor/dist/jsoneditor.min.css";
import lodash from "lodash";

interface Props {
    modelValue: JsonValue;
    readOnly?: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits(["update:modelValue"]);

const editor = useTemplateRef<HTMLElement>("editor");
let instance: JSONEditor | undefined;
let isUserTyping = false;

const handleChangeText = lodash.debounce(newValue => {
    isUserTyping = true;
    try {
        const parsed = newValue ? JSON.parse(newValue) : ({} as JsonValue);
        emit("update:modelValue", parsed);
    } catch (error) {
        console.error("JSON 解析错误:", error);
    }
}, 300);

onMounted(() => {
    if (editor.value) {
        instance = new JSONEditor(editor.value, {
            mode: "code",
            allowSchemaSuggestions: true,
            indentation: 4,
            mainMenuBar: false,
            statusBar: false,
            onChangeText: handleChangeText
        });

        // 初始化时设置值
        if (props.modelValue) {
            instance.setText(JSON.stringify(props.modelValue, undefined, 2));
        }
    }

    // 如果有只读属性，则设置为只读模式
    if (props.readOnly && instance) {
        instance.setMode("view");
    }
});

watch(
    () => props.modelValue,
    newVal => {
        if (instance && newVal && editor.value?.contains(document.activeElement) === false && !isUserTyping) {
            instance.setText(JSON.stringify(newVal, undefined, 2));
        }
        isUserTyping = false;
    },
    { deep: true }
);

watch(
    () => props.readOnly,
    val => {
        if (instance) {
            instance.setMode(val ? "view" : "code");
        }
    }
);
</script>

<template>
    <div ref="editor"></div>
</template>
