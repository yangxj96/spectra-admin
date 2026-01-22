import { ref, watch } from "vue";
import { authApi } from "@/api/auth/auth.ts";
import { GlobalUtils } from "@/utils/global-utils.ts";
import { useDebounceFn, useIdle } from "@vueuse/core";
import { MessageUtils } from "@/utils/message-utils.ts";
import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";

export function useAuthIdle({
    idleTime = 10 * 60 * 1000, // 10分钟空闲
    refreshDelay = 1000 // Token刷新防抖
} = {}) {
    const userStore = useUserStore();
    const isIdle = ref(false);
    let stopIdle: (() => void) | null = null;

    // 防抖刷新Token
    const refreshToken = useDebounceFn(async () => {
        if (userStore.token?.access_token) {
            try {
                await authApi.check(); // 调用后端 /check 刷新 TTL
            } catch (err) {
                console.error("Token刷新失败", err);
            }
        }
    }, refreshDelay);

    // 登录后启动空闲监听
    const start = () => {
        if (stopIdle) stopIdle(); // 避免重复启动

        const { idle, stop } = useIdle(idleTime, {
            events: ["mousemove", "mousedown", "keydown", "scroll", "touchstart"]
        });
        stopIdle = stop;

        watch(idle, async idleValue => {
            isIdle.value = idleValue;

            if (idleValue && userStore.token?.access_token) {
                // 用户空闲，自动登出
                await authApi.logout();
                MessageUtils.success("长时间未操作，已自动退出", () => {
                    GlobalUtils.exit();
                });
            } else if (!idleValue) {
                // 用户活跃，延迟刷新 Token
                refreshToken();
            }
        });
    };

    // 登出时停止监听
    const stop = () => {
        stopIdle?.();
        stopIdle = null;
    };

    return {
        isIdle,
        start,
        stop
    };
}
