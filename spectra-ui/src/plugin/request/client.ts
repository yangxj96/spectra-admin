import { request } from "./http";

export function createClient<TParams, TResult>(url: string, method: "GET" | "POST" | "PUT" | "DELETE") {
    return (params?: TParams) => {
        if (method === "GET" || method === "DELETE") {
            return request<TResult>(url, {
                method,
                params: params as Record<string, unknown>
            });
        }

        return request<TResult>(url, {
            method,
            body: JSON.stringify(params)
        });
    };
}
