import { defineStore } from "pinia";

const usePropertiesStore = defineStore("props", {
    state: (): StoreProps => ({
        personal_details: false,
        change_password: false
    })
});

export default usePropertiesStore;
