import { defineStore } from "pinia";

export const usePropsStore = defineStore("props", {
    state: (): StoreProps => ({
        personal_details: false,
        change_password: false
    })
});
