import type { DeviceApi } from "./types";

export const deviceApi: DeviceApi = {
    getDeviceId() {
        return "android-device-id";
    },

    getSystem() {
        return "Android";
    }
};
