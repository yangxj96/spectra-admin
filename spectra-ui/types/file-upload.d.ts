export {};

declare global {
    // 预处理请求参数
    type FilePreprocessFrom = {
        filename: string;
        size: number;
        hash: string;
    };

    // 预处理请求参数
    type FilePreprocessVO = {
        // 是否存在
        exists: boolean;
        // 请求地址
        url: string;
        // 是否需要分片上传
        multipart: boolean;
        // 上传ID
        upload_id: string;
        // 分片大小
        chunk_size: number;
    };

    type SingleParams = {
        // 文件
        file: File;
        // hash值
        hash: string;
        // 上传ID
        upload_id: string;
    };

    type ChunkParams = {
        // 文件
        file: File;
        // 文件名称
        filename: string;
        // hash值
        hash: string;
        // 上传ID
        upload_id: string;
        // 分片大小
        chunk_size: number;
    };
}
