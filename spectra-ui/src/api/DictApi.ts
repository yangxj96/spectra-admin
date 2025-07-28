import http from "@/plugin/request";

export default {
    ///////////////////////////////// 字典组
    // 创建字典组
    async createGroup(params: DictGroup): Promise<IResult> {
        return await http.post<IResult>("/api/dict/group", params).then(res => res.data);
    },
    // 删除字典组
    async deleteGroupById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/dict/group/${id}`).then(res => res.data);
    },
    // 修改字典组
    async modifyGroup(params: DictGroup): Promise<IResult> {
        return await http.put<IResult>("/api/dict/group", params).then(res => res.data);
    },
    // 获取字典组Tree
    async getTypesGroupTree(): Promise<IResult<DictTypeTree[]>> {
        return await http.get<IResult<DictTypeTree[]>>("/api/dict/group/tree").then(res => res.data);
    },
    ///////////////////////////////// 字典项
    // 创建字典项
    async createData(params: DictData): Promise<IResult> {
        return await http.post<IResult>("/api/dict/data", params).then(res => res.data);
    },
    // 删除字典项
    async deleteDataById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/dict/data/${id}`).then(res => res.data);
    },
    // 修改字典项
    async modifyData(params: DictData): Promise<IResult> {
        return await http.put<IResult>("/api/dict/data", params).then(res => res.data);
    },
    // 根据字典组CODE获取字典项列表
    async getDataByTypeCode(code: string): Promise<IResult<DictData[]>> {
        return await http.get<IResult<DictData[]>>(`/api/dict/data/${code}`).then(res => res.data);
    }
};
