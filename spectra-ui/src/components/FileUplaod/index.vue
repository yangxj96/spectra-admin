<script setup lang="ts">
import { ref } from "vue";
import FileUploadApi from "@/api/FileUploadApi.ts";
import type { UploadFile, UploadFiles, UploadProgressEvent, UploadUserFile } from "element-plus";

const fileList = ref<UploadUserFile[]>([
    {
        name: "food.jpeg",
        url: "https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100"
    },
    {
        name: "food2.jpeg",
        url: "https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100"
    }
]);

/**
 * 文件上传时的钩子
 * @param evt
 * @param uploadFile
 * @param uploadFiles
 */
const handleProgress = (evt: UploadProgressEvent, uploadFile: UploadFile, uploadFiles: UploadFiles) => {
    console.log(evt, uploadFile, uploadFiles);
    FileUploadApi.preprocess(fileList);
};
</script>

<template>
    <el-dialog>
        <el-upload v-model:file-list="fileList" action="#" multiple :on-progress="handleProgress" list-type="picture">
            <el-button type="primary">点击选择文件</el-button>
            <template #tip>
                <div class="el-upload__tip">jpg/png files with a size less than 500kb</div>
            </template>
        </el-upload>
    </el-dialog>
</template>
