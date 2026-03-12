import { get } from "@/plugin/request/api.ts";

/**
 * 服务器信息相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const serviceMonitorApi = {
    /**
     * 获取CPU信息
     */
    getCPUInfo(): Promise<CPUInfo> {
        return get<CPUInfo>("/api/service/monitor/getCPUInfo");
    },
    /**
     * 获取内存信息
     */
    getRAMInfo(): Promise<RAMInfo> {
        return get<RAMInfo>("/api/service/monitor/getRAMInfo");
    },
    /**
     * 获取JVM信息
     */
    getJVMInfo(): Promise<JVMInfo> {
        return get<JVMInfo>("/api/service/monitor/getJVMInfo");
    }
};
