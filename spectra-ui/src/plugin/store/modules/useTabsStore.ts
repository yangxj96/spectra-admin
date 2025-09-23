import { defineStore } from "pinia";

const useTabsStore = defineStore("tabs", {
    state: (): StoreTabs => ({
        tabs: [],
        active: undefined
    })
});
export default useTabsStore;
