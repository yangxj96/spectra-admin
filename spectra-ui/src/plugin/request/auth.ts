import { useUserStore } from "@/plugin/store/modules/use-user-store";

let refreshing = false;
let queue: (() => void)[] = [];

export function getToken(): string | null {
    const token = useUserStore().token.access_token;
    return token || null;
}

export async function refreshToken(): Promise<string | null> {
    if (refreshing) {
        return new Promise(resolve => {
            queue.push(() => resolve(getToken()));
        });
    }

    refreshing = true;

    try {
        const res = await fetch("/auth/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!res.ok) {
            return null;
        }

        const data = await res.json();

        const store = useUserStore();

        store.token.access_token = data.token;

        queue.forEach(cb => cb());
        queue = [];

        return data.token;
    } catch {
        return null;
    } finally {
        refreshing = false;
    }
}
