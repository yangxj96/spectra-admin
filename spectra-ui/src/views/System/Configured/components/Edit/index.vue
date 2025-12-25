<script setup lang="ts">
import Icons from "@/components/Icons/index.vue";
// 是否显示
const show = defineModel("show", {
    type: Boolean,
    required: true,
    default: false
});

// 具体表单
const form = defineModel<Configured>("form", {
    type: Object,
    required: true,
    default: () => ({})
});

// 定义响应方法
const emits = defineEmits(["close"]);

const handleDrawerClose = () => {
    show.value = false;
    emits("close");
};
</script>

<template>
    <!-- 配置编辑 -->
    <el-drawer v-model="show" :modal="true" modal-penetrable destroy-on-close @close="handleDrawerClose">
        <template #header>
            <div>
                <icons name="icon-edit" />
                编辑配置
            </div>
        </template>

        <template #default>
            <el-watermark style="height: 100%; width: 100%">
                <el-form ref="formRef" :model="form" label-width="auto" @submit.prevent>
                    <el-form-item label="ID" prop="id">
                        <el-text>{{ form.id }}</el-text>
                    </el-form-item>
                    <el-form-item label="配置键" prop="key">
                        <el-input v-if="form.id" v-model="form.key" placeholder="请输入配置键" />
                        <el-text v-else>{{ form.key }}</el-text>
                    </el-form-item>
                    <el-form-item label="配置值" prop="value">
                        <el-input v-model="form.value" placeholder="请输入配置值" />
                    </el-form-item>
                    <el-form-item label="备注" prop="remarks">
                        <el-input v-model="form.remarks" type="textarea" :rows="5" placeholder="请输入配置说明" />
                    </el-form-item>
                </el-form>
            </el-watermark>
        </template>

        <template #footer>
            <el-button @click="handleDrawerClose">取消</el-button>
            <el-button type="primary">确定</el-button>
        </template>
    </el-drawer>
</template>
