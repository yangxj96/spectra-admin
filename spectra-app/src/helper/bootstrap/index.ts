import useAppStore from "@/stores/app";
import { initConfig } from "./config";
import { initUser } from "./user";

export async function bootstrap() {
    const appStore = useAppStore();

    try {
        await initConfig();
        await initUser();
    } catch (e) {
        console.error("bootstrap error:", e);
    }

    appStore.setReady(true);
}
