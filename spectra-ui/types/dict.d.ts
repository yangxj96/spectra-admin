export {};

declare global {
    // 字典组
    type DictGroup = BaseEntity & {
        //父级ID
        pid: string;
        //字典类型名称
        name: string;
        //字典类型编码
        code: string;
        //状态
        state: number;
        //备注
        remark?: string;
        //是否内置
        builtin?: boolean;
        //是否隐藏
        hide?: boolean;
    };

    // 字典组树形结构
    type DictTypeTree = DictGroup & {
        // 下级内容
        children?: DictTypeTree[];
    };

    // 字典数据
    type DictItem = BaseEntity & {
        // 字典组ID
        gid: string;
        // 字典标签
        label: string;
        // 字典值
        value: string;
        // 排序
        sort: number;
        // 状态
        state: number;
        // 是否默认
        default_flag: boolean;
        // 备注
        remark?: string;
    };
}
