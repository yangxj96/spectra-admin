import http from "@/plugin/request";

/**
 * 文件上传相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-12-18 00:28:33
 */
export default {
    /**
     * 文件预处理
     * @param params 上传文件入参
     */
    async preprocess(params: {}): Promise<IResult> {
        return await http
            .get<IResult>("/api/file/preprocess", {
                data: params
            })
            .then(res => res.data);
    },
    /**
     * 查询上传信息
     * @param hash 文件hash值
     */
    async progress(hash: string): Promise<IResult> {
        return await http
            .get<IResult>("/api/file/progress", {
                data: { hash }
            })
            .then(res => res.data);
    },
    /**
     * 文件上传(小文件)
     * @param params 上传文件入参
     */
    async upload(params: {}): Promise<IResult> {
        return http.postForm("/api/file/upload", params).then(res => res.data);
    },
    /**
     * 上传文件(切片)
     * @param params 上传文件入参
     */
    async chunk(params: {}): Promise<IResult> {
        return http.postForm("/api/file/chunk", params).then(res => res.data);
    }
};
