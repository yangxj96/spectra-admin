<script setup lang="ts">
import { ref, useTemplateRef } from "vue";
import { FileUtils } from "@/utils/file-utils.ts";
import { MessageUtils } from "@/utils/message-utils.ts";
import { fileUploadApi } from "@/api/common/file-upload.ts";

const fileList = ref<FileItem[]>([]);

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

// 文件选中的时候修改文件
const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (!target.files) {
        return;
    }
    for (let file of target.files) {
        if (!file) {
            continue;
        }

        fileList.value.push({
            name: file.name,
            size: file.size,
            status: 0,
            file: file
        });
    }
};

// 文件上传被单击
const handleFileUploadClick = async () => {
    if (fileList.value.length <= 0) {
        MessageUtils.warning("文件列表为空");
        return;
    }
    for (let file of fileList.value) {
        await handlePreFileUpload(file.file);
    }
};

// 文件预处理
const handlePreFileUpload = async (file: File) => {
    let pre_params = await preprocess(file);
    if (!pre_params) {
        MessageUtils.error("预处理文件错误");
        return;
    }
    let pre_res = await fileUploadApi.preprocess(pre_params);
    if (pre_res.code !== 200) {
        MessageUtils.error(pre_res.msg);
        return;
    }
    // 已存在,跳过,应该还要处理下UI
    if (pre_res.data?.has_exist) {
        handleExistFile(file);
    }
    // 是否要进行分片上传
    if (pre_res.data?.has_chunked) {
        await handleLargeFileUpload(file, pre_params.hash, pre_res.data.size);
    } else {
        await handleSmallFileUpload(file, pre_params.hash);
    }
};

// 处理文件已存在
const handleExistFile = (file: File) => {
    console.log("已存在了", file);
};

// 处理小文件上传
const handleSmallFileUpload = async (file: File, hash: string) => {
    // 直接上传
    let upload_params = new FormData();
    upload_params.append("file", file);
    upload_params.append("hash", hash);

    let upload_res = await fileUploadApi.upload(upload_params);
    console.log("直接上传", upload_res);
};

// 处理大文件上传
const handleLargeFileUpload = async (file: File, hash: string, size: number) => {
    let chunks = createChunks(file, size);
    let idx = 0;
    for (let chunk of chunks) {
        let chunk_params = new FormData();
        chunk_params.append("file", chunk);
        chunk_params.append("fileName", file.name);
        chunk_params.append("hash", hash);
        chunk_params.append("index", idx.toString());
        chunk_params.append("count", chunks.length.toString());
        idx++;
        let chunk_res = await fileUploadApi.chunk(chunk_params);
        console.log("分片上传", chunk_res);
    }
};

// 创建分片
const createChunks = (file: File, chunkSize: number) => {
    const chunks: Blob[] = [];
    let start = 0;
    while (start < file.size) {
        const end = Math.min(start + chunkSize, file.size);
        chunks.push(file.slice(start, end));
        start = end;
    }
    return chunks;
};

// 计算文件预处理结果
const preprocess = async (file: File) => {
    if (file) {
        return {
            filename: file.name,
            size: file.size,
            hash: await FileUtils.hash(file)
        } as FilePreprocessFrom;
    } else {
        return {} as FilePreprocessFrom;
    }
};
</script>

<template>
    <el-dialog v-model="model" width="30%" style="min-height: 40vh">
        <el-row>
            <el-button type="primary" @click="handleSelectFile">选择文件</el-button>
            <el-button type="primary" @click="handleFileUploadClick" :disabled="fileList.length <= 0">
                开始上传
            </el-button>
            <input
                ref="fileInputRef"
                type="file"
                multiple
                style="display: none"
                @change="handleFileChange"
                accept=".jpg,.png,.pdf,.docx" />
        </el-row>
        <el-row style="min-height: 35vh">
            <el-table :data="fileList" height="35vh" style="width: 100%" class="loading-box">
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
