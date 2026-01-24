<script setup lang="ts">
defineOptions({
    name: "LayoutsFooter"
});
</script>

<template>
    <!-- 底部版权 -->
    <el-footer class="footer">
        Copyright © 2025
        <strong><a href="https://www.devops00.com" target="_blank">devops00</a></strong>
        &nbsp;
        <strong><a href="https://www.devops00.com" target="_blank">devops00.com</a></strong>
        All Rights Reserved. 备案号：
        <a target="_blank" rel="nofollow" href="https://beian.miit.gov.cn/">滇ICP备2025012345号-1</a>
    </el-footer>
</template>

<style scoped lang="scss">
.footer {
    text-align: center;
    width: 100%;
    height: 20px;
    font-size: 10px;
    line-height: 20px;
    color: var(--el-text-color-regular);

    a {
        color: var(--el-text-color-primary);
    }
}
</style>
