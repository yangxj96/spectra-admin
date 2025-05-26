/**
 * 路由字段定义
 */
type Route = BaseEntity & {
    /**地址**/
    uri: string;
    /**排序**/
    order: number;
    /**判断语**/
    predicates: string;
    /**过滤器**/
    filters: string;
    /**元数据**/
    metadata: string;
    /**路由ID**/
    route_id: string;
};
