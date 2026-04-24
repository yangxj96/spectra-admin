import type { AppApi } from "./types";

let api: AppApi;

// #ifdef APP-ANDROID || APP-HARMONY
import { appApi as androidApi } from "./android";
api = androidApi;
// #endif

// #ifdef WEB
import { appApi as webApi } from "./web";
api = webApi;
// #endif

/**
 * 对外统一导出
 */
export const app = api;
