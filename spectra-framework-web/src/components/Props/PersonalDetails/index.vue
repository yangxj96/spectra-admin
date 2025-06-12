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
                <icons name="icon-user" />
                个人信息
            </span>
        </template>

        <el-row style="width: 100%; height: 100%">
            <el-col :span="20" :push="2">
                <img
                    src="@/assets/images/avatar.png"
                    alt="default avatar"
                    style="object-fit: cover; margin-left: 80px; margin-bottom: 10px"
                    class="el-avatar el-avatar--circle el-tooltip__trigger" />
                <el-form label-width="80px">
                    <el-form-item label="用户名">
                        <el-input value="我是用户名" :disabled="true" placeholder="请输入用户名" />
                    </el-form-item>
                    <el-form-item label="邮箱">
                        <el-input placeholder="请输入邮箱" />
                    </el-form-item>
                    <el-form-item label="手机号码">
                        <el-input placeholder="请输入手机号码" />
                    </el-form-item>
                    <el-form-item label="个人说明">
                        <el-input placeholder="请输入个人说明" type="textarea" rows="5" />
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

<script lang="ts">
import { defineComponent } from "vue";
import usePropsStore from "@/plugin/store/modules/usePropsStore";

export default defineComponent({
    name: "PropsPersonalDetails",
    data() {
        return {
            isShow: usePropsStore().personal_details
        };
    },
    created() {
        usePropsStore().$subscribe((_, state) => {
            /*
      * mutation主要包含三个属性值：
      *   events：当前state改变的具体数据，包括改变前的值和改变后的值等等数据
      *   storeId：是当前store的id
      *   type：用于记录这次数据变化是通过什么途径，主要有三个分别是
      *         “direct” ：通过 action 变化的
                ”patch object“ ：通过 $patch 传递对象的方式改变的
                “patch function” ：通过 $patch 传递函数的方式改变的
      *
      * */
            // 我们就可以在此处监听store中值的变化，当变化为某个值的时候，去做一些业务操作之类的
            this.isShow = state.personal_details;
        });
    },
    methods: {
        // 关闭弹窗
        handlePropsCancel() {
            usePropsStore().personal_details = false;
        }
    }
});
</script>
