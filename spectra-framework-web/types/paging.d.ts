/**
 * 基础分页请求参数
 */
type BasePageParams = {
    page_size: number;
    page_num: number;
};

/**
 * 用户分页请求参数定义
 */
type UserPageParams = BasePageParams & {
    username?: string;
    telephone?: string;
    status?: string;
};

/**
 * 菜单分页请求参数定义
 */
type MenuPageParams = BasePageParams & {
    name?: string;
};

/**
 * 路由分页请求参数定义
 */
type RoutePageParams = BasePageParams & {
    uri?: string;
    route_id?: string;
};

/**
 * 角色分页请求参数定义
 */
type RolePageParams = BasePageParams & {
    /** 角色名称 **/
    name?: string;
    /**
     * 角色状态
     */
    state?: boolean;
};

/**
 * 分页实体
 */
type Page<T = never> = {
    current: number;
    optimize_count_sql: boolean;
    orders?: string[];
    pages: number;
    records: T[];
    search_count: boolean;
    size: number;
    total: number;
};

/**
 * 分页参数实体
 */
type Pagination = {
    size: number;
    page: number;
    page_sizes: Array<number>;
    default_page_size: number;
    total: number;
};
