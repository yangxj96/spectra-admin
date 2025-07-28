<script lang="ts">
import { defineComponent } from "vue";
import usePropsStore from "@/plugin/store/modules/usePropsStore";

export default defineComponent({
    name: "PropsChangePassword",
    data() {
        return {
            isShow: usePropsStore().change_password,
            form: {
                old_password: "",
                new_password: "",
                verify_password: ""
            },
            rules: {
                old_password: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
                new_password: [{ required: true, message: "请输入新密码", trigger: "blur" }],
                verify_password: [{ required: true, message: "请重复输入新密码", trigger: "blur" }]
            }
        };
    },
    created() {
        usePropsStore().$subscribe((_, state) => {
            this.isShow = state.change_password;
        });
    },
    methods: {
        handlePropsCancel() {
            usePropsStore().change_password = false;
        }
    }
});
</script>

<template>
    <el-dialog
        v-model="isShow"
        :destroy-on-close="true"
        :append-to-body="true"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
        width="30%">
        <template #header>
            <span>
                <icons name="icon-reset-password" />
                修改密码
            </span>
        </template>

        <el-row style="width: 100%; height: 100%">
            <el-col :span="20" :push="2">
                <el-form v-model="form" :rules="rules" label-width="80px">
                    <el-form-item label="旧密码" prop="old_password">
                        <el-input v-model="form.old_password" placeholder="请输入原来的密码" />
                    </el-form-item>
                    <el-form-item label="新密码" prop="new_password">
                        <el-input v-model="form.new_password" placeholder="请输入新的密码" />
                    </el-form-item>
                    <el-form-item label="重复密码" prop="verify_password">
                        <el-input v-model="form.verify_password" placeholder="请重复输入新密码" />
                    </el-form-item>
                </el-form>
            </el-col>
        </el-row>

        <template #footer>
            <slot name="footer">
                <el-button @click="handlePropsCancel">关闭</el-button>
                <el-button type="primary">确定</el-button>
            </slot>
        </template>
    </el-dialog>
</template>
