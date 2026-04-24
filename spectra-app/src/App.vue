<script setup lang="ts">
import { onExit, onHide, onLaunch, onShow } from "@dcloudio/uni-app";
import { pushManager } from "./helper/push_message";
import useAppStore from "./stores/app";

onLaunch(() => {
    console.log("App启动");
    const appStore = useAppStore();
    // 获取客户端推送ID
    uni.getPushClientId({
        success: res => {
            console.log("获取推送ClientId成功：", res.cid);
            appStore.setPushId(res.cid);
        },
        fail: err => {
            console.error("获取推送ClientId失败：", err);
        }
    });
    // 接收客户端推送消息
    // 可选：订阅自定义事件
    pushManager.on("receive", data => {
        console.log("收到消息回调:", data);
    });

    pushManager.on("click", data => {
        console.log("点击通知回调:", data);
    });
});
onShow(() => {
    console.log("App切换到前台");
});
onHide(() => {
    console.log("App切换到后台");
});
onExit(() => {
    console.log("App退出");
    // 取消监听推送消息
    uni.offPushMessage();
});
</script>
<style></style>
