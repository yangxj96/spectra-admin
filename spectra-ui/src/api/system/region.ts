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
        return get<Region[]>("/api/region/lazy", params, { loading: false });
    },
    /**
     * 分页查询系统配置信息
     * @param params 分页参数
     */
    page(params?: RegionPageParams): Promise<Page<Region>> {
        return get<Page<Region>>("/api/region/page", params);
    },
    /**
     * 根据ID查询路径信息
     * @param id id
     */
    path(id: string): Promise<RegionPathVO> {
        return get<RegionPathVO>(`/api/region/path/${id}`, {}, { loading: false });
    }
};
