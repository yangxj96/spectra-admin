/**
 * 请求方法类型
 */
export type RequestMethod = "OPTIONS" | "HEAD" | "GET" | "POST" | "PUT" | "DELETE" | "TRACE" | "CONNECT";

/**
 * 请求配置
 */
export type RequestOptions = {
    url: string;
    method?: RequestMethod;
    data?: any;
    header?: Record<string, string>;
    timeout?: number;
};

/**
 * 请求响应
 */
export type RequestResult<T> = {
    code: number;
    data: T | null;
    msg: string;
};
