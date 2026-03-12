import { del, get, post, put } from "@/plugin/request/api";

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
    createGroup(params: DictGroup): Promise<void> {
        return post<void>("/api/dict/group", params);
    },
    /**
     * 删除字典组
     * @param id 字典组ID
     */
    deleteGroupById(id: string): Promise<void> {
        return del<void>(`/api/dict/group/${id}`);
    },
    /**
     * 修改字典组
     * @param params 字典组入参
     */
    updateGroup(params: DictGroup): Promise<void> {
        return put<void>("/api/dict/group", params);
    },
    /**
     * 获取字典组Tree
     */
    getTypesGroupTree(): Promise<DictTypeTree[]> {
        return get<DictTypeTree[]>("/api/dict/group/tree");
    },
    ///////////////////////////////// 字典项
    /**
     * 创建字典项
     * @param params 字典项入参
     */
    createData(params: DictItem): Promise<void> {
        return post<void>("/api/dict/data", params);
    },
    /**
     * 删除字典项
     * @param id 字典项ID
     */
    deleteDataById(id: string): Promise<void> {
        return del<void>(`/api/dict/data/${id}`);
    },
    /**
     * 修改字典项
     * @param params 字典项入参
     */
    updateData(params: DictItem): Promise<void> {
        return put<void>("/api/dict/data", params);
    },
    /**
     * 根据字典组CODE获取字典项列表
     * @param code 字典组CODE
     */
    getDataByTypeCode(code: string): Promise<DictItem[]> {
        return get<DictItem[]>(`/api/dict/data/${code}`);
    }
};
