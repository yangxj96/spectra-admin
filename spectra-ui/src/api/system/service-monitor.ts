import http from "@/plugin/request/index.ts";

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
    async getCPUInfo(): Promise<IResult<CPUInfo>> {
        return await http.get<IResult<CPUInfo>>("/api/service/monitor/getCPUInfo").then(res => res.data);
    },
    /**
     * 获取内存信息
     */
    async getRAMInfo(): Promise<IResult<RAMInfo>> {
        return await http.get<IResult<RAMInfo>>("/api/service/monitor/getRAMInfo").then(res => res.data);
    },
    /**
     * 获取JVM信息
     */
    async getJVMInfo(): Promise<IResult<JVMInfo>> {
        return await http.get<IResult<JVMInfo>>("/api/service/monitor/getJVMInfo").then(res => res.data);
    }
};
