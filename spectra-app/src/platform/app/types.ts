/**
 * App 基础信息（平台能力）
 */
export type AppInfo = {
    appId?: string;
    version: string;
    name?: string;
};

/**
 * App 能力接口（统一抽象）
 */
export type AppApi = {
    /**
     * 获取 App 信息
     */
    getAppInfo(): Promise<AppInfo>;

    /**
     * 退出应用
     */
    exitApp(): void;

    /**
     * 处理返回键（双击退出）
     */
    handleBackPress(): void;
};
