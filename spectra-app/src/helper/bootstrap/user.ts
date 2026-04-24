import useAppStore from "@/stores/app";

export async function initUser() {
    const appStore = useAppStore();

    // 从本地获取 token
    const token = uni.getStorageSync("token");

    if (!token) {
        console.log("未登录");
        return;
    }

    appStore.setToken(token);

    // 模拟获取用户信息
    await new Promise(resolve => setTimeout(resolve, 300));

    appStore.setUser({
        name: "Jack",
        id: 1
    });

    console.log("用户初始化完成");
}
