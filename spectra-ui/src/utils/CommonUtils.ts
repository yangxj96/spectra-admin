import * as SunCalc from "suncalc";

export default class CommonUtils {
    /**
     * 获取随机数
     * @param min 最小数
     * @param max 最大数
     */
    public static getRandom(min: number, max: number) {
        return Math.floor(Math.random() * (max - min)) + min;
    }

    /**
     * 生成UUID
     * @constructor
     */
    public static UUID() {
        const randomBytes = new Uint8Array(16);
        crypto.getRandomValues(randomBytes);
        // 设置版本号（第 7 位为 4）
        randomBytes[6] = (randomBytes[6] & 0x0f) | 0x40;
        // 设置变体号（第 9 位为 8, 9, a, b）
        randomBytes[8] = (randomBytes[8] & 0x3f) | 0x80;
        // 将字节数组转换为十六进制字符串
        const hex = [...randomBytes].map(byte => byte.toString(16).padStart(2, "0")).join("");
        // 插入分隔符
        return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
    }

    /**
     * 生成UUID(大写)
     * @constructor
     */
    public static UUIDUpper() {
        return this.UUID().toUpperCase();
    }

    /**
     * 生成UUID(小写)
     * @constructor
     */
    public static UUIDLower() {
        return this.UUID().toLowerCase();
    }

    /**
     * 根据经纬度获取日出日落时间后进行判断是否需要进入深色模式
     */
    public static shouldEnableDarkMode() {
        let lat = 25.526_473_000_000_014;
        let lon = 103.792_161_999_999_96;
        const now = new Date();
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                lat = position.coords.latitude;
                lon = position.coords.longitude;
            });
        }
        //sunrise = 日出 sunset = 日落
        const { sunrise, sunset } = SunCalc.getTimes(now, lat, lon);
        // 日出前 或 日落后，开启深色模式
        return now < sunrise || now >= sunset;
    }
}
