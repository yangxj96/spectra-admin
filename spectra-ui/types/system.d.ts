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
    //子级
    children?: Menu[];
};

// 部门
type Organization = BaseEntity & {
    // 上级ID
    pid: string;
    // 名称
    name: string;
    // 代码
    code: string;
    // 类型
    type: number;
    // 地址
    address: string;
    // 负责人ID
    manager_id: string;
    // 备注
    remark?: string;
};

// 组织机构树形
type OrganizationTree = Organization & {
    // 子级
    children?: OrganizationTree[];
};

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
type DictData = BaseEntity & {
    //字典组ID
    gid: string;
    //字典标签
    label: string;
    //字典值
    value: string;
    //排序
    sort: number;
    //状态
    state: number;
    //备注
    remark?: string;
};
