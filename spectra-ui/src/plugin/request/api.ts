import { request } from "./http";

/**
 * GET
 */
export function get<T, U extends string = string>(
    url: U,
    params?: Record<string, unknown>,
    options?: RequestOptions<U>
) {
    return request<T, U>(url, {
        method: "GET",
        params,
        ...options
    });
}

/**
 * POST
 */
export function post<T, U extends string = string>(url: U, data?: unknown, options?: RequestOptions<U>) {
    return request<T, U>(url, {
        method: "POST",
        body: JSON.stringify(data),
        ...options
    });
}

/**
 * PUT
 */
export function put<T, U extends string = string>(url: U, data?: unknown, options?: RequestOptions<U>) {
    return request<T, U>(url, {
        method: "PUT",
        body: JSON.stringify(data),
        ...options
    });
}

/**
 * DELETE
 */
export function del<T, U extends string = string>(
    url: U,
    params?: Record<string, unknown>,
    options?: RequestOptions<U>
) {
    return request<T, U>(url, {
        method: "DELETE",
        params,
        ...options
    });
}

/**
 * 上传文件
 */
export function upload<T, U extends string = string>(url: U, form: FormData, options?: RequestOptions<U>) {
    // 参数合规转换
    const newForm = transformFormData(form);
    return request<T, U>(url, {
        method: "POST",
        body: newForm,
        ...options
    });
}

/**
 * 下载文件
 */

export async function download<U extends string = string>(url: U, options?: RequestOptions<U>) {
    return await request<Blob, U>(url, {
        method: "GET",
        ...options
    });
}

function snakeToCamel(str: string) {
    return str.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
}

function transformFormData(form: FormData) {
    const newForm = new FormData();

    form.forEach((value, key) => {
        const newKey = snakeToCamel(key);
        newForm.append(newKey, value);
    });

    return newForm;
}
