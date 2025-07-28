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
