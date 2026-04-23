import { get, put } from "@/plugin/request/api.ts";

export const configuredApi = {
    // 分页查询系统配置信息
    page(): Promise<Page<ConfiguredPageVO>> {
        return get<Page<ConfiguredPageVO>>("/api/configured/page");
    },
    // 修改系统配置
    upload(params: ConfiguredDTO): Promise<void> {
        return put<void>("/api/configured", params);
    }
};
