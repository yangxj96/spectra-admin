// 需要拦截的方法
const methods = ["navigateTo", "redirectTo", "reLaunch", "switchTab"];

// 白名单
const whiteList = [
    "/pages/splash/index", // 启动页
    "/pages/login/index" // 登录页
];

/**
 * 判断是否需要登录
 */
function needLogin(url: string) {
    return !whiteList.some(item => url.startsWith(item));
}

let isRedirecting = false;

/**
 * 检查登陆情况,未登录跳转login页面
 */
export function checkToken(url?: string): boolean {
    // 白名单放行
    if (url && !needLogin(url)) return true;

    // 开发阶段暂时放行
    console.log("开发阶段暂时放行", url);
    return true;

    // const token = uni.getStorageSync("token");

    // // 已登录
    // if (token) return true;

    // // 防止重复跳转
    // if (isRedirecting) return false;

    // isRedirecting = true;

    // uni.reLaunch({
    //     url: `/pages/login/index?redirect=${safeEncode(url)}`
    // });

    // return false;
}

/**
 * 安全编码URL，避免重复编码导致的问题
 */
function safeEncode(url?: string) {
    if (!url) return "";

    try {
        // 先解一次，避免重复编码
        const decoded = decodeURIComponent(url);
        return encodeURIComponent(decoded);
    } catch {
        return encodeURIComponent(url);
    }
}

// 监听器, 监听到 navigateTo 事件
methods.forEach(method => {
    uni.addInterceptor(method, {
        invoke(args) {
            if (!checkToken(args.url)) {
                return false;
            }
            return args;
        }
    });
});
