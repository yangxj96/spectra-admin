import { post, upload } from "@/plugin/request/api.ts";

/**
 * 文件上传相关接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-12-18 00:28:33
 */
export const fileUploadApi = {
    /**
     * 文件预处理
     * @param params 上传文件入参
     */
    pre(params: FilePreprocessFrom): Promise<FilePreprocessVO> {
        return post<FilePreprocessVO>("/api/file/pre", params, { loading: false });
    },
    /**
     * 文件上传(小文件)
     * @param params 上传文件入参
     */
    async uploadSingle(params: FormData): Promise<void> {
        return upload<void>("/api/file/uploadSingle", params, { loading: false });
    },
    /**
     * 上传文件(切片)
     * @param params 上传文件入参
     */
    async uploadChunk(params: FormData): Promise<void> {
        return upload<void>("/api/file/uploadChunk", params, { loading: false, dedupe: false });
    },
    /**
     * 等待文件合并
     * @param upload_id 上传ID
     */
    async merge(upload_id: string): Promise<string> {
        await post<void>(`/api/file/merge/${upload_id}`, undefined, { loading: false, dedupe: false });
        return "";
    }
};
