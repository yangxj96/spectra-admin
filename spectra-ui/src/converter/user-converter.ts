/**
 * 用户类型转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026-04-22 00:00:00
 */
export const userConverter = {
    createForm(): UserForm {
        return {
            id: "",
            username: "",
            real_name: "",
            status: undefined,
            gender: undefined,
            birthday: "",
            phone: "",
            email: "",
            country: "",
            city: "",
            language: "",
            timezone: "",
            department_id: "",
            role_ids: [],
            data_scope: undefined,
            target_ids: []
        };
    },
    // 转到表单回显用
    toForm(datum: UserPageVO): UserForm {
        return {
            // 基础字段
            id: datum.id ?? "",
            username: datum.username ?? "",
            real_name: datum.real_name ?? "",
            status: datum.status ?? undefined,
            gender: datum.gender ?? undefined,
            birthday: datum.birthday ?? "",
            phone: datum.phone ?? "",
            email: datum.email ?? "",
            country: datum.country ?? "",
            city: datum.city ?? "",
            language: datum.language ?? "",
            timezone: datum.timezone ?? "",
            department_id: datum.department_id ?? "",
            role_ids: datum.roles?.map(r => r.id) ?? [],
            data_scope: datum.data_scope ?? undefined,
            target_ids: datum.target_ids ?? []
        };
    },
    // 转成表单请求参数
    toDTO(form: UserForm): UserDTO {
        return {
            id: form.id,
            username: form.username,
            real_name: form.real_name,
            status: form.status ?? undefined,
            gender: form.gender ?? undefined,
            birthday: form.birthday ?? undefined,
            phone: form.phone,
            email: form.email,
            country: form.country,
            city: form.city,
            language: form.language,
            timezone: form.timezone,
            department_id: form.department_id,
            role_ids: form.role_ids ?? [],
            data_scope: form.data_scope ?? undefined,
            // 根据 data_scope 控制
            target_ids: form.data_scope === 4 ? (form.target_ids ?? []) : []
        };
    }
};
