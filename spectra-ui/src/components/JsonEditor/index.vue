<script setup lang="ts">
import JSONEditor from "jsoneditor";
import "jsoneditor/dist/jsoneditor.min.css";
import { onMounted, useTemplateRef, watch } from "vue";

defineOptions({
    name: "JsonEditor"
});

interface Props {
    modelValue?: JsonValue;
    readOnly?: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
    (e: "update:modelValue", value: JsonValue): void;
}>();

const editor = useTemplateRef<HTMLElement>("editor");
let instance: JSONEditor | undefined;
let isUserTyping = false;

let debounceTimer: ReturnType<typeof setTimeout> | null = null;
const handleChangeText = (newValue: string) => {
    if (debounceTimer) {
        clearTimeout(debounceTimer);
    }

    debounceTimer = setTimeout(() => {
        isUserTyping = true;

        try {
            const parsed = newValue ? JSON.parse(newValue) : {};
            emit("update:modelValue", parsed);
        } catch (error) {
            console.error("JSON 解析错误:", error);
        }
    }, 300);
};

/**
 * 安全 stringify
 */
function safeStringify(value: JsonValue | undefined) {
    try {
        return JSON.stringify(value ?? {}, null, 2);
    } catch {
        return "{}";
    }
}

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
            instance.setText(safeStringify(props.modelValue));
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
        if (!instance) return;

        // 避免用户正在输入时覆盖
        const isFocused = editor.value?.contains(document.activeElement);

        if (!isFocused && !isUserTyping) {
            instance.setText(safeStringify(newVal));
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
