/**
 * 部门类型转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026-04-22 00:00:00
 */
export const DeptConverter = {
    createForm(): Department {
        return {
            id: "",
            pid: "",
            name: "",
            code: "",
            type: 0,
            region_id: "",
            region_name: "",
            path: "",
            sort: 0
        };
    }
};
