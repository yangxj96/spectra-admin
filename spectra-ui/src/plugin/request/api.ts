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
export function upload<T, U extends string = string>(url: U, file: File, field = "file", options?: RequestOptions<U>) {
    const form = new FormData();
    form.append(field, file);
    return request<T, U>(url, {
        method: "POST",
        body: form,
        ...options
    });
}

/**
 * 下载文件
 */

export async function download<U extends string = string>(url: U, options?: RequestOptions<U>) {
    const blob = await request<Blob, U>(url, {
        method: "GET",
        ...options
    });

    return blob;
}
