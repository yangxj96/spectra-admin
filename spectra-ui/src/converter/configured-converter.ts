/**
 * 系统配置类型转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026-04-22 00:00:00
 */
export const configuredConverter = {
    createForm(): ConfiguredForm {
        return {
            id: "",
            key: "",
            value: undefined,
            type: "",
            dict_code: "",
            remarks: ""
        };
    },

    toForm(datum: ConfiguredPageVO): ConfiguredForm {
        return {
            id: datum.id ?? "",
            key: datum.key ?? "",
            value: datum.value ?? undefined,
            type: datum.type ?? "",
            dict_code: datum.dict_code ?? "",
            remarks: datum.remarks ?? ""
        };
    },

    toDTO(datum: ConfiguredForm): ConfiguredDTO {
        return {
            id: datum.id ?? "",
            key: datum.key ?? "",
            value: datum.value ?? undefined,
            type: datum.type ?? "",
            dict_code: datum.dict_code ?? "",
            remarks: datum.remarks ?? ""
        };
    }
};
