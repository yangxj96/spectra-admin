import http from "@/plugin/request";

/**
 * 行政区域相关接口
 */
export const regionApi = {
    /**
     * 获取行政区域
     *
     * @param params 查询条件
     */
    async load(params: { level: number; id?: string }): Promise<IResult<Region[]>> {
        return http
            .get<IResult<Region[]>>("/api/region", {
                params
            })
            .then(res => res.data);
    }
};
