import { defineStore } from "pinia";

export interface UserState {
    token: Token;
}

const useUserStore = defineStore("user", {
    state: () => {
        return {
            token: {}
        } as UserState;
    },
    persist: true
});

export default useUserStore;
