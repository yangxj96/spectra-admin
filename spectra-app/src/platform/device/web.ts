import type { DeviceApi } from "./types";

export const deviceApi: DeviceApi = {
    getDeviceId() {
        return "ios-device-id";
    },

    getSystem() {
        return "iOS";
    }
};
