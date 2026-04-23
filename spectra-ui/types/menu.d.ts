export {};

declare global {
    // 菜单元数据
    type MenuMetadata = {
        // 已知
        title?: string;
        // 字段兜底
        [key: string]: JsonValue;
    };

    // 菜单
    type Menu = BaseEntity & {
        //父级ID
        pid: string;
        //图标
        icon: string;
        //名称
        name: string;
        //路径
        path: string;
        //组件地址
        component: string;
        //排序
        sort: number;
        //布局
        layout?: string;
        //模块
        module?: string;
        //参数
        params?: never;
        //元数据
        meta?: never;
        // 是否显示菜单
        hide?: boolean;
        // 元数据
        metadata?: MenuMetadata;
        //子级
        children?: Menu[];
    };
}
