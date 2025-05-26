import { defineStore } from "pinia";
import zhCn from "element-plus/es/locale/lang/zh-cn";

const useAppStore = defineStore("app", {
    state: () => ({
        lang: zhCn,
        menus: [] as Menu[],
        unfold: true
    }),
    persist: true
});

export default useAppStore;
