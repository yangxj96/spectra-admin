import http from "@/plugin/request/index.ts";

export default {
    // 获取CPU信息
    async getCPUInfo() {
        return await http.get<IResult<CPUInfo>>("/api/service/monitor/getCPUInfo").then(res => res.data);
    },

    // 获取内存信息
    async getRAMInfo() {
        return await http.get<IResult<RAMInfo>>("/api/service/monitor/getRAMInfo").then(res => res.data);
    },

    // 获取JVM信息
    async getJVMInfo() {
        return await http.get<IResult<JVMInfo>>("/api/service/monitor/getJVMInfo").then(res => res.data);
    }
};
