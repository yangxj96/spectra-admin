export default {
    /**
     * 退出程序需要的处理内容
     */
    exit() {
        globalThis.localStorage.clear();
        globalThis.sessionStorage.clear();
        location.reload();
    }
};
