// 文件列表
type FileItem = {
    name: string;
    size: number;
    status: number;
    file: File;
};

// 预处理请求参数
type FilePreprocessFrom = {
    filename: string;
    size: number;
    hash: string;
};

// 预处理请求参数
type FilePreprocessVO = {
    has_exist: boolean;
    has_chunked: boolean;
    size: number;
    count: number;
};
