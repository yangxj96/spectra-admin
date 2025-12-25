import { defineStore } from "pinia";
import zhCn from "element-plus/es/locale/lang/zh-cn";

const useAppStore = defineStore("app", {
    state: (): StoreApp => ({
        lang: zhCn,
        menus: [] as Menu[],
        isFetchingMenus: false,
        unfold: true,
        watermark: false
    })
});

export default useAppStore;
