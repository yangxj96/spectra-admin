export {};

declare global {
    // 部门树形响应
    type DepartmentTreeVO = {
        // 主键ID
        id: string;
        // 上级ID
        pid?: string;
        // 名称
        name: string;
        // 代码
        code: string;
        // 类型
        type: number;
        // 行政区划ID
        region_id: string;
        // 行政区划名称
        region_name: string;
        // 路径
        path: string;
        // 排序
        sort: number;
        // 备注
        remark?: string;
        // 下级部门
        children?: DepartmentTreeVO[];
    };

    // 部门表单编辑类型
    type DepartmentForm = {
        // 主键ID
        id: string;
        // 上级ID
        pid: string;
        // 名称
        name: string;
        // 代码
        code: string;
        // 类型
        type: number | undefined;
        // 行政区划ID
        region_id: string;
        // 行政区划名称
        region_name: string;
        // 路径
        path: string;
        // 排序
        sort: number | undefined;
        // 备注
        remark: string;
    };

    // 部门编辑请求类型
    type DepartmentDTO = {
        // 主键ID
        id: string;
        // 上级ID
        pid: string;
        // 名称
        name: string;
        // 代码
        code: string;
        // 类型
        type: number | undefined;
        // 行政区划ID
        region_id: string;
        // 路径
        path: string;
        // 排序
        sort: number | undefined;
        // 备注
        remark: string;
    };
}
