import { get } from "@/plugin/request/api.ts";
/**
 * 行政区域相关接口
 */
export const regionApi = {
    /**
     * 获取行政区域
     *
     * @param params 查询条件
     */
    load(params: { level: number; id?: string }): Promise<Region[]> {
        return get<Region[]>("/api/region", params);
    },
    // 分页查询系统配置信息
    page(params?: RegionPageParams): Promise<Page<Region>> {
        return get<Page<Region>>("/api/region/page", params);
    }
};
