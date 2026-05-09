/**
 * 角色类型转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026-04-23 00:00:00
 */
export const roleConverter = {
    createForm(): RoleForm {
        return {
            // 主键ID
            id: "",
            // 角色名称
            name: "",
            // 角色代码
            code: "",
            // 角色状态
            state: undefined,
            // 角色范围
            scope: undefined,
            // 自定义范围ID列表
            target_ids: [],
            // 是否内置
            builtin: undefined,
            // 角色备注
            remark: ""
        };
    },
    toForm(datum: RolePageVO): RoleForm {
        return {
            id: datum.id ?? "",
            name: datum.name ?? "",
            code: datum.code ?? "",
            state: datum.state ?? undefined,
            scope: datum.scope ?? undefined,
            target_ids: datum.target_ids ?? [],
            builtin: datum.builtin ?? undefined,
            remark: datum.remark ?? ""
        };
    },
    toDTO(datum: RoleForm): RoleDTO {
        return {
            id: datum.id ?? "",
            name: datum.name ?? "",
            code: datum.code ?? "",
            state: datum.state ?? undefined,
            scope: datum.scope ?? undefined,
            target_ids: datum.target_ids ?? [],
            builtin: datum.builtin ?? undefined,
            remark: datum.remark ?? ""
        };
    }
};
