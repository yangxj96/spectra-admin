/**
 * 部门类型转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026-04-22 00:00:00
 */
export const deptConverter = {
    createForm(): DepartmentForm {
        return {
            id: "",
            pid: "",
            name: "",
            code: "",
            type: undefined,
            region_id: "",
            region_name: "",
            path: "",
            sort: undefined,
            remark: ""
        };
    },
    toForm(datum: DepartmentTreeVO): DepartmentForm {
        return {
            id: datum.id ?? "",
            pid: datum.pid ?? "",
            name: datum.name ?? "",
            code: datum.code ?? "",
            type: datum.type ?? undefined,
            region_id: datum.region_id ?? "",
            region_name: datum.region_name ?? "",
            path: datum.path ?? "",
            sort: datum.sort ?? undefined,
            remark: datum.remark ?? ""
        };
    },
    toDTO(datum: DepartmentForm): DepartmentDTO {
        return {
            id: datum.id ?? "",
            pid: datum.pid ?? "",
            name: datum.name ?? "",
            code: datum.code ?? "",
            type: datum.type ?? undefined,
            region_id: datum.region_id ?? "",
            path: datum.path ?? "",
            sort: datum.sort ?? undefined,
            remark: datum.remark ?? ""
        };
    }
};
