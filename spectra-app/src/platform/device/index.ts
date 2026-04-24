import type { DeviceApi } from "./types";

let api: DeviceApi;

// #ifdef APP-ANDROID || APP-HARMONY
import { deviceApi as androidApi } from "./android";

api = androidApi;

// #endif
// #ifdef WEB
import { deviceApi as webApi } from "./web";

api = webApi;
// #endif

export const device = api;
