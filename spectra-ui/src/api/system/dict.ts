import http from "@/plugin/request";

/**
 * 字典相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11 15:00:00
 */
export const dictApi = {
    ///////////////////////////////// 字典组
    /**
     * 创建字典组
     * @param params 字典组入参
     */
    async createGroup(params: DictGroup): Promise<IResult> {
        return await http.post<IResult>("/api/dict/group", params).then(res => res.data);
    },
    /**
     * 删除字典组
     * @param id 字典组ID
     */
    async deleteGroupById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/dict/group/${id}`).then(res => res.data);
    },
    /**
     * 修改字典组
     * @param params 字典组入参
     */
    async modifyGroup(params: DictGroup): Promise<IResult> {
        return await http.put<IResult>("/api/dict/group", params).then(res => res.data);
    },
    /**
     * 获取字典组Tree
     */
    async getTypesGroupTree(): Promise<IResult<DictTypeTree[]>> {
        return await http.get<IResult<DictTypeTree[]>>("/api/dict/group/tree").then(res => res.data);
    },
    ///////////////////////////////// 字典项
    /**
     * 创建字典项
     * @param params 字典项入参
     */
    async createData(params: DictItem): Promise<IResult> {
        return await http.post<IResult>("/api/dict/data", params).then(res => res.data);
    },
    /**
     * 删除字典项
     * @param id 字典项ID
     */
    async deleteDataById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/dict/data/${id}`).then(res => res.data);
    },
    /**
     * 修改字典项
     * @param params 字典项入参
     */
    async modifyData(params: DictItem): Promise<IResult> {
        return await http.put<IResult>("/api/dict/data", params).then(res => res.data);
    },
    /**
     * 根据字典组CODE获取字典项列表
     * @param code 字典组CODE
     */
    async getDataByTypeCode(code: string): Promise<IResult<DictItem[]>> {
        return await http.get<IResult<DictItem[]>>(`/api/dict/data/${code}`).then(res => res.data);
    }
};
