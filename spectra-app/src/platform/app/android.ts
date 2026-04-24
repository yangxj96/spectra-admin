import { createBaseAppApi } from "./base";
import type { AppApi, AppInfo } from "./types";

let firstBackTime = 0;
const DURATION = 2000;

export const appApi: AppApi = {
    ...createBaseAppApi(),
    getAppInfo(): Promise<AppInfo> {
        return new Promise(resolve => {
            plus.runtime.getProperty(plus.runtime.appid!, inf => {
                resolve({
                    appId: plus.runtime.appid,
                    version: (inf.version + "").replace("\n", ""),
                    name: inf.name
                });
            });
        });
    },
    exitApp(): void {
        plus.runtime.quit();
    },

    handleBackPress(): void {
        const now = Date.now();

        if (firstBackTime === 0) {
            uni.showToast({
                title: "再按一次退出应用",
                position: "bottom"
            });

            firstBackTime = now;

            setTimeout(() => {
                firstBackTime = 0;
            }, DURATION);
        } else if (now - firstBackTime < DURATION) {
            firstBackTime = 0;
            plus.runtime.quit();
        }
    }
};
