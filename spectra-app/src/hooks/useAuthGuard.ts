import { checkToken } from "@/interceptor/route";
import { onShow } from "@dcloudio/uni-app";

export function useAuthGuard() {
    // 只在微信小程序生效
    // #ifdef MP-WEIXIN
    onShow(() => {
        const pages = getCurrentPages();
        const current = pages[pages.length - 1];
        const url = "/" + current.route; // 当前页面路径
        checkToken(url);
    });
    // #endif
}
