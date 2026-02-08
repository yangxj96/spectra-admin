export {};

declare global {
    // useAppStore()的state
    type StoreApp = {
        // 全局i18n语言
        lang: object;
        // 菜单列表
        menus: Menu[];
        // 当前菜单子级.
        currentMenus: Menu[];
        // 当前菜单前缀
        currentMenusPrefix: string;
        // 是否展开菜单
        unfold: boolean;
        // 防止重复请求
        isFetchingMenus: boolean;
        // 水印
        watermark: boolean;
    };

    // usePropsStore()的state
    type StoreProps = {
        // 用户详情
        personal_details: boolean;
        // 修改密码
        change_password: boolean;
    };

    // useUserStore()的state
    type StoreUser = {
        // 用户登录的token
        token: Token;
        // 是否登录
        isLoggedIn: boolean;
    };

    // useDictStore()的state
    type StoreDict = {
        // 字典缓存数组
        dicts: Record<string, DictItem[]>;
    };
}
