<script setup lang="ts">
import { ref } from "vue";
import Icons from "@/components/Icons/index.vue";

const drawer = ref(true);

const configuredForm = ref({ id: 1000001, key: "system.xx", value: "true", remarks: "是否开启水印" });

const table_data = ref([
    { id: 1000001, key: "system.xx", value: "true", remarks: "是否开启水印" },
    { id: 1000001, key: "system.xx", value: "true", remarks: "是否开启水印" },
    { id: 1000001, key: "system.xx", value: "true", remarks: "是否开启水印" },
    { id: 1000001, key: "system.xx", value: "true", remarks: "是否开启水印" }
]);

</script>

<template>
    <!-- 搜索区 -->
    <el-row class="box-search">
        <el-form :inline="true">
            <el-form-item label="菜单名称" prop="name">
                <el-input placeholder="请输入菜单名称" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary">查询</el-button>
                <el-button>重置</el-button>
            </el-form-item>
        </el-form>
    </el-row>
    <!-- 数据区 -->
    <el-row class="box-body">
        <el-table :data="table_data" height="100%" stripe default-expand-all row-key="id">
            <el-table-column align="center" prop="id" label="主键" />
            <el-table-column align="center" prop="key" label="配置键" />
            <el-table-column align="center" prop="value" label="配置值" />
            <el-table-column align="center" prop="remarks" label="备注" />
            <el-table-column align="center" label="操作">
                <template #default="">
                    <el-button v-owner="'MENU:UPDATE'" link type="primary" size="small">编辑</el-button>
                </template>
            </el-table-column>
        </el-table>
    </el-row>
    <!-- 配置编辑 -->
    <el-drawer v-model="drawer" :modal="true" modal-penetrable destroy-on-close >
        <template #header>
            <div>
                <icons name="icon-edit" />
                编辑配置
            </div>
        </template>

        <template #default>
            <el-watermark style="height: 100%; width: 100%">
                <el-form ref="formRef" :model="configuredForm" label-width="auto" @submit.prevent>
                    <el-form-item label="ID" prop="id">
                        <el-text >{{ configuredForm.id }}</el-text>
                    </el-form-item>
                    <el-form-item label="配置键" prop="key">
                        <el-text >{{ configuredForm.key }}</el-text>
                    </el-form-item>
                    <el-form-item label="配置值" prop="value">
                        <el-input v-model="configuredForm.value" placeholder="请输入配置值" />
                    </el-form-item>
                    <el-form-item label="备注" prop="remarks">
                        <el-input v-model="configuredForm.remarks" type="textarea" :rows="5" placeholder="请输入配置说明" />
                    </el-form-item>
                </el-form>
            </el-watermark>
        </template>

        <template #footer>
            <el-button >取消</el-button>
            <el-button type="primary" >确定</el-button>
        </template>
    </el-drawer>
</template>

<style scoped lang="scss">
.box-search {
    height: 10%;
    display: flex;
    align-items: center;
    padding-left: 20px;
}

.box-body {
    height: 90%;
}
</style>
