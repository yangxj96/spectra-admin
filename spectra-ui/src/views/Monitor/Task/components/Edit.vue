<script setup lang="ts">
import { ref } from "vue";

import ComponentsIcons from "@/components/ComponentsIcons/index.vue";
import JsonEditor from "@/components/JsonEditor/index.vue";

const visible = ref(false);

const rules = {
    name: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    bean: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    method: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    cron: [{ required: true, message: "请输入用户名", trigger: "blur" }],
    status: [{ required: true, message: "请输入用户名", trigger: "blur" }]
};
</script>

<template>
    <el-dialog
        v-model="visible"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        :destroy-on-close="true"
        width="35vw">
        <template #header>
            <ComponentsIcons name="icon-edit" />
            新增定时任务
        </template>
        <template #default>
            <el-form :rules="rules" label-width="auto">
                <el-form-item label="任务名称" prop="name">
                    <el-input />
                </el-form-item>
                <el-form-item label="BEAN名称" prop="bean">
                    <el-input />
                </el-form-item>
                <el-form-item label="执行方法" prop="method">
                    <el-input />
                </el-form-item>
                <el-form-item label="参数" prop="args">
                    <JsonEditor :read-only="false" :model-value="{}" style="width: 100%" />
                </el-form-item>
                <el-form-item label="cron" prop="cron">
                    <el-input />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group>
                        <el-radio value="0">运行</el-radio>
                        <el-radio value="1">停止</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="描述" prop="remarks">
                    <el-input type="textarea" :rows="5" />
                </el-form-item>
            </el-form>
        </template>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
                <el-button type="primary" @click="visible = false">确认</el-button>
            </div>
        </template>
    </el-dialog>
</template>
