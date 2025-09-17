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
const router = useRouter();

const time = ref<number>(-1);
const second = ref<number>(3);

onMounted(() => {
    time.value = globalThis.setInterval(() => {
        second.value = second.value - 1;
        if (second.value <= 0) {
            router.push("/");
        }
    }, 1000);
});

onUnmounted(() => {
    clearTimeout(time.value);
});

function handleBack() {
    if (time.value != -1) {
        clearTimeout(time.value);
    }
    router.back();
}
</script>

<template>
    <el-result icon="error" title="404" :sub-title="`对不起,找不到页面,${second}秒后自动后退`">
        <template #extra>
            <el-button type="primary" @click="handleBack">后退</el-button>
        </template>
    </el-result>
</template>
