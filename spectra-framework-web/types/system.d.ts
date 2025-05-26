/**
 * 菜单
 */
type Menu = BaseEntity & {
    /**
     * 父级ID
     */
    pid: string;
    /**
     * 图标
     */
    icon: string;
    /**
     * 名称
     */
    name: string;
    /**
     * 路径
     */
    path: string;
    /**
     * 组件地址
     */
    component: string;
    /**
     * 排序
     */
    sort: number;
    /**
     * 布局
     */
    layout?: string;
    /**
     * 模块
     */
    module?: string;
    /**
     * 参数
     */
    params?: never;
    /**
     * 元数据
     */
    meta?: never;
    /**
     * 子级
     */
    children?: Menu[];
};
