import { defineStore } from "pinia";

export const useTabsStore = defineStore("tabs", {
    state: (): StoreTabs => ({
        tabs: [],
        active: undefined
    })
});
