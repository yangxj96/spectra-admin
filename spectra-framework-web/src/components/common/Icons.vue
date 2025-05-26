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
    /* 因icon大小被设置为和字体大小一致，而span等标签的下边缘会和字体的基线对齐，故需设置一个往下的偏移比例，来纠正视觉上的未对齐效果 */
    vertical-align: -0.1em;
    /* 定义元素的颜色，currentColor是一个变量，这个变量的值就表示当前元素的color值，如果当前元素未设置color值，则从父元素继承 */
    fill: currentColor;
    overflow: hidden;
    width: 1em;
    height: 1em;
}
</style>
