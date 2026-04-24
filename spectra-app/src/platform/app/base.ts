import type { AppApi } from "./types";

/**
 * 创建基础公用部分
 */
export function createBaseAppApi(): AppApi {
    return {
        getAppInfo() {
            return Promise.resolve({
                appId: "",
                version: "",
                name: ""
            });
        },
        exitApp() {
            // #ifdef APP-ANDROID || APP-HARMONY
            plus.runtime.quit();
            // #endif
        },
        handleBackPress() {}
    };
}
