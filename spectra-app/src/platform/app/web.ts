import { createBaseAppApi } from "./base";
import type { AppApi } from "./types";

export const appApi: AppApi = {
    ...createBaseAppApi(),
    exitApp(): void {
        console.warn("web没有退出应用");
    },
    handleBackPress() {
        console.warn("web无此功能");
    }
};
