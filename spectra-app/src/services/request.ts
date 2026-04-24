import { request } from "./http";

export function get<T>(url: string, data?: any, header?: Record<string, string>) {
    return request<T>({
        url,
        method: "GET",
        data,
        header
    });
}

export function post<T>(url: string, data?: any, header?: Record<string, string>) {
    return request<T>({
        url,
        method: "POST",
        data,
        header
    });
}
