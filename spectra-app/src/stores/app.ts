import { defineStore } from "pinia";

const useAppStore = defineStore("app", {
    state: () => ({
        ready: false,
        token: "",
        userInfo: null as any,
        isFirstLaunch: true,
        // 推送ID
        push_id: ""
    }),

    actions: {
        setReady(val: boolean) {
            this.ready = val;
        },
        setToken(token: string) {
            this.token = token;
        },
        setUser(user: any) {
            this.userInfo = user;
        },
        setFirstLaunch(val: boolean) {
            this.isFirstLaunch = val;
        },
        setPushId(id: string) {
            this.push_id = id;
        }
    }
});

export default useAppStore;
