import { defineStore } from "pinia";

const useUserStore = defineStore("user", {
    state: (): StoreUser => {
        return {
            token: {} as Token
        };
    },
    persist: true
});

export default useUserStore;
