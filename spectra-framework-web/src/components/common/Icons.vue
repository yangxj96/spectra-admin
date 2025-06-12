<template>
    <svg :class="svgClass" aria-hidden="true">
        <use class="svg-use" v-bind="{ 'xlink:href': symbolId }" />
    </svg>
</template>

<script setup lang="ts">
import { computed } from "vue";

// 定义 props
const props = defineProps({
    prefix: {
        type: String,
        default: "icon"
    },
    name: {
        type: String,
        required: true
    },
    className: {
        type: String,
        default: ""
    }
});

// 计算属性
const symbolId = computed(() => `#${props.name}`);
const svgClass = computed(() => {
    if (props.className) {
        return `svg-icon ${props.className}`;
    }
    return "svg-icon";
});
</script>

<style scoped lang="scss">
.svg-icon {
    vertical-align: -0.1em;
    fill: currentColor;
    overflow: hidden;
    width: 1em;
    height: 1em;
}

// 透明边框
.svg-icon:focus {
    /* 使用透明边框或其他颜色 */
    outline: 2px solid transparent;
    /* 可选：设置轮廓线与对象之间的空间 */
    outline-offset: 2px;
}
</style>
