// useAppStore()的state
type StoreApp = {
    // 全局i18n语言
    lang: object;
    // 菜单列表
    menus: Menu[];
    // 是否展开菜单
    unfold: boolean;
};

// usePropsStore()的state
type StoreProps = {
    // 用户详情
    personal_details: boolean;
    // 修改密码
    change_password: boolean;
};

// useTabsStore()的state
type StoreTabs = {
    // 选项卡存储的数组,不包含首页这个固定项
    tabs: string[];
    // 当前激活的选项卡
    active: string | undefined;
};

// useUserStore()的state
type StoreUser = {
    // 用户登录的token
    token: Token;
};

// useDictStore()的state
type StoreDict = {
    // 字典缓存数组
    dicts: Record<string, DictData[]>;
};
