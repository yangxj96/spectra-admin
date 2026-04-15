import * as SunCalc from "suncalc-ts";

export const CommonUtils = {
    /**
     * 获取随机数
     * @param min 最小数
     * @param max 最大数
     */
    getRandom(min: number, max: number) {
        return Math.floor(Math.random() * (max - min)) + min;
    },
    /**
     * 生成UUID
     * @constructor
     */
    UUID(): string {
        if (typeof crypto !== "undefined" && crypto.getRandomValues) {
            const randomBytes = new Uint8Array(16);
            crypto.getRandomValues(randomBytes);

            // 使用 ! 断言 randomBytes 不为 undefined（实际不可能）
            randomBytes[6] = (randomBytes[6]! & 0x0f) | 0x40;
            randomBytes[8] = (randomBytes[8]! & 0x3f) | 0x80;

            const hex = Array.from(randomBytes, byte => byte.toString(16).padStart(2, "0")).join("");
            return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
        } else {
            // 回退方案
            return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replaceAll(/[xy]/g, c => {
                const r = Math.trunc(Math.random() * 16);
                const v = c === "x" ? r : (r & 0x3) | 0x8;
                return v.toString(16);
            });
        }
    },
    /**
     * 生成UUID(大写)
     * @constructor
     */
    UUIDUpper() {
        return this.UUID().toUpperCase();
    },
    /**
     * 生成UUID(小写)
     * @constructor
     */
    UUIDLower() {
        return this.UUID().toLowerCase();
    },
    /**
     * 根据经纬度获取日出日落时间后进行判断是否需要进入深色模式
     */
    shouldEnableDarkMode() {
        const lat = 25.526_473_000_000_014;
        const lon = 103.792_161_999_999_96;
        const now = new Date();
        //sunrise = 日出 sunset = 日落
        const { sunrise, sunset } = SunCalc.getTimes(now, lat, lon);
        // 日出前 或 日落后，开启深色模式
        return now < sunrise || now >= sunset;
    },
    /**
     * 是否是刷新进入
     */
    hasReload() {
        // 判断是否是刷新进来的
        const navigationEntries = globalThis.performance?.getEntriesByType?.("navigation");
        const navigationEntry = navigationEntries?.[0] as PerformanceNavigationTiming | undefined;
        if (navigationEntry?.type === "reload") {
            sessionStorage.setItem("reloaded", "true");
        }
    }
};
