import { request } from "./http";

/**
 * GET
 */
export function get<T>(url: string, params?: Record<string, unknown>, options?: RequestOptions) {
    return request<T>(url, {
        method: "GET",
        params,
        ...options
    });
}

/**
 * POST
 */
export function post<T>(url: string, data?: unknown, options?: RequestOptions) {
    return request<T>(url, {
        method: "POST",
        body: JSON.stringify(data),
        ...options
    });
}

/**
 * PUT
 */
export function put<T>(url: string, data?: unknown, options?: RequestOptions) {
    return request<T>(url, {
        method: "PUT",
        body: JSON.stringify(data),
        ...options
    });
}

/**
 * DELETE
 */
export function del<T>(url: string, params?: Record<string, unknown>, options?: RequestOptions) {
    return request<T>(url, {
        method: "DELETE",
        params,
        ...options
    });
}

/**
 * 上传文件
 */
export function upload<T>(url: string, file: File, field = "file", options?: RequestOptions) {
    const form = new FormData();

    form.append(field, file);

    return request<T>(url, {
        method: "POST",
        body: form,
        ...options
    });
}

/**
 * 下载文件
 */
export async function download(url: string, params?: Record<string, unknown>, options?: RequestOptions) {
    const blob = await request<Blob>(url, {
        method: "GET",
        params,
        ...options
    });

    return blob;
}
