import { defineStore } from "pinia";

const usePropertiesStore = defineStore("props", {
    state: () => ({
        personal_details: false,
        change_password: false
    }),
    persist: true
});

export default usePropertiesStore;
