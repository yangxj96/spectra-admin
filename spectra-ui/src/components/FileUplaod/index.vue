<script setup lang="ts">
import FileUtils from "@/utils/FileUtils.ts";
import { ref, useTemplateRef } from "vue";

// {
// name: "file1",
//     size: 109999,
//     status: 0,
//     file: new Blob()
// }

const fileList = ref<{}[]>([]);

/**
 * 是否展示model
 */
const model = defineModel({
    type: Boolean,
    required: true
});

// 隐藏的文件选择
const fileInputRef = useTemplateRef("fileInputRef");

// 选择文件
const handleSelectFile = () => {
    if (fileInputRef.value) {
        fileInputRef.value.value = "";
        fileInputRef.value.click();
    }
};

const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (!target.files) {
        return
    }
    for (let file of target.files) {
        fileList.value.push({
            name: file?.name,
            size: file?.size,
            status: 0,
            file: file
        });
    }
};

// 计算文件预处理结果
const preprocess = async (file: File) => {
    try {
        if (file) {
            let hash = await FileUtils.hash(file);
            console.log("hash:", hash, "hash长度:", hash.length);
            return {
                filename: file.name,
                size: file.size,
                hash: await FileUtils.hash(file)
            };
        } else {
            return {};
        }
    } catch (error) {
        console.log(error);
    }
};
</script>

<template>
    <el-dialog v-model="model" width="30%">
        <el-row>
            <el-button type="primary" @click="handleSelectFile">选择文件</el-button>
            <el-button type="primary" :disabled="fileList.length <= 0">开始上传</el-button>

            <input
                ref="fileInputRef"
                type="file"
                multiple
                style="display: none"
                @change="handleFileChange"
                accept=".jpg,.png,.pdf,.docx" />
        </el-row>
        <el-row>
            <el-table :data="fileList" style="width: 100%">
                <el-table-column align="center" width="060" type="index" label="序号" />
                <el-table-column align="center" label="文件名称" prop="name" />
                <el-table-column align="center" width="150" label="文件大小" prop="size">
                    <template #default="scope">{{ scope.row.size }} Byte</template>
                </el-table-column>
                <el-table-column align="center" width="100" label="上传状态" prop="status">
                    <template #default="scope">
                        <el-tag v-if="scope.row.status === 0" type="warning">等待上传</el-tag>
                        <el-tag v-if="scope.row.status === 1" type="primary">上传中...</el-tag>
                        <el-tag v-if="scope.row.status === 2" type="success">上传完成</el-tag>
                    </template>
                </el-table-column>
            </el-table>
        </el-row>
    </el-dialog>
</template>
