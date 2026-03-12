export {};

declare global {
    type ExtractPathParams<T extends string> = T extends `${string}{${infer Param}}${infer Rest}`
        ? Param | ExtractPathParams<Rest>
        : never;

    type PathParams<T extends string> =
        ExtractPathParams<T> extends never ? undefined : Record<ExtractPathParams<T>, string | number>;

    /**
     * 请求优先级
     */
    export type HttpPriority = "high" | "normal" | "low";

    export interface RequestOptions<T extends string> extends RequestInit {
        /**
         * 请求参数
         */
        params?: Record<string, unknown>;

        /**
         * 是否显示 loading
         */
        loading?: boolean;

        /**
         * 网络失败重试次数
         */
        retry?: number;

        /**
         * 是否启用缓存
         */
        cache?: boolean;

        /**
         * 是否启用请求去重
         */
        dedupe?: boolean;

        /**
         * 应用层优先级（控制并发队列）
         */
        priority?: HttpPriority;

        /**
         * 浏览器网络优先级
         */
        fetchPriority?: RequestPriority;

        /**
         * 路径参数
         */
        pathParams?: PathParams<T>;

        /**
         * 内部字段：token 刷新标记
         */
        _retry?: boolean;
    }

    // 响应整体
    type IResult<T = unknown> = {
        // 状态码
        code: number;
        // 消息
        msg: string;
        // 响应内容
        data?: T;
    };

    // 基础实体都有的类型
    type BaseEntity = {
        // 主键ID
        id: string;
        // 创建人
        created_user?: string;
        // 创建时间
        created_time?: string;
        // 最后更新人
        updated_user?: string;
        // 最后更新时间
        updated_time?: string;
    };
}
