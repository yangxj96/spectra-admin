<script setup lang="ts">
import { fileUploadApi } from "@/api/common/file-upload.ts";
import { CommonUtils } from "@/utils/common-utils.ts";
import { MessageUtils } from "@/utils/message-utils.ts";

import type { UploadRequestOptions } from "element-plus";

defineOptions({
    name: "FileUpload"
});

/**
 * 处理文件上传
 */
const handleUpload = async (options: UploadRequestOptions) => {
    console.log(options);
    const hash = CommonUtils.UUIDUpper();
    // 先进行预处理
    const { exists, url, multipart, upload_id, chunk_size } = await fileUploadApi.pre({
        filename: options.file.name,
        hash: hash,
        size: options.file.size
    });
    console.log(`存在:${exists},地址:${url},是否需要分片:${multipart},上传id:${upload_id},分片大小:${chunk_size}`);
    // 存在则秒传,提示成功
    if (exists) {
        MessageUtils.success("上传成功");
        return;
    }
    if (multipart) {
        uploadChunk(options.file, options.file.name, hash, upload_id, chunk_size);
    } else {
        await uploadSingle(options.file, hash, upload_id);
    }
};

//////////// 辅助方法

/**
 * 普通上传
 */
const uploadSingle = async (file: File, hash: string, upload_id: string) => {
    const params = new FormData();
    params.append("file", file);
    params.append("hash", hash);
    params.append("upload_id", upload_id);
    await fileUploadApi.uploadSingle(params);
};

/**
 * 分片上传
 */
const uploadChunk = async (file: File, filename: string, hash: string, upload_id: string, chunk_size: number) => {
    const chunks = createChunks(file, chunk_size);
    const tasks = chunks.map((chunk, index) => {
        const params = new FormData();
        params.append("file", chunk!);
        params.append("upload_id", upload_id);
        params.append("file_name", filename);
        params.append("hash", hash);
        params.append("count", chunks.length.toString());
        params.append("index", (index + 1).toString());

        return fileUploadApi.uploadChunk(params);
    });

    // ✅ 等所有成功
    await Promise.all(tasks);

    // ✅ 再 merge
    await fileUploadApi.merge(upload_id);
};

/**
 * 创建分片
 * @param file
 * @param size
 */
const createChunks = (file: File, size: number): Blob[] => {
    const result: Blob[] = [];
    let cur = 0;
    while (cur < file.size) {
        result.push(file.slice(cur, cur + size));
        cur += size;
    }
    return result;
};
</script>

<template>
    <el-upload action="#" :http-request="handleUpload" :auto-upload="true" :multiple="true">
        <el-button type="primary">上传</el-button>
    </el-upload>
</template>
