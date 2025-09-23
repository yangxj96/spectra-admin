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
