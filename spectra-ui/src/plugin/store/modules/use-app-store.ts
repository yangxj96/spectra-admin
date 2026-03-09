import zhCn from "element-plus/es/locale/lang/zh-cn";
import { defineStore } from "pinia";

export const useAppStore = defineStore("app", {
    state: (): StoreApp => ({
        lang: zhCn,
        menus: [],
        currentMenus: [],
        currentMenusPrefix: "",
        isFetchingMenus: false,
        unfold: true,
        watermark: false
    })
});
