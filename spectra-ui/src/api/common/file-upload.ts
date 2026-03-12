import { get, upload } from "@/plugin/request/api.ts";

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
    preprocess(params: FilePreprocessFrom): Promise<FilePreprocessVO> {
        return get<FilePreprocessVO>("/api/file/preprocess", params);
    },
    /**
     * 查询上传信息
     * @param hash 文件hash值
     */
    async progress(hash: string): Promise<void> {
        return get<void>("/api/file/progress", { hash });
    },
    /**
     * 文件上传(小文件)
     * @param params 上传文件入参
     */
    async upload(params: FormData): Promise<void> {
        return upload<void>("/api/file/upload", params);
    },
    /**
     * 上传文件(切片)
     * @param params 上传文件入参
     */
    async chunk(params: FormData): Promise<void> {
        return upload<void>("/api/file/chunk", params);
    }
};
