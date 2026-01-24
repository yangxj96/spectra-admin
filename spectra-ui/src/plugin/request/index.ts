import qs from "qs";
import axios, { type AxiosError, type AxiosResponse, type Canceler, type InternalAxiosRequestConfig } from "axios";
import { GlobalUtils } from "@/utils/global-utils.ts";
import { MessageUtils } from "@/utils/message-utils.ts";
import { hideLoading, showLoading } from "@/plugin/element/loading";
import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";

// 常见内容类型
// application/x-www-form-urlencoded
// application/json

const http = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 60 * 1000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
        "Api-Version": "1.0.0"
    },
    /**
     * 自定义 paramsSerializer，用于 axios 请求参数的序列化，支持嵌套对象和数组。
     * @param params - 请求参数对象（支持嵌套对象和数组）
     * @returns 序列化后的查询字符串
     */
    paramsSerializer: (params: Record<string, unknown>): string => {
        // 使用 qs.stringify 处理嵌套对象和数组
        return qs.stringify(params, {
            // 支持数组格式化，默认为 brackets 格式
            arrayFormat: "indices",
            // 是否使用点符号表示嵌套对象，默认为 false
            allowDots: true,
            encoder: (str: string) => {
                return encodeURIComponent(str);
            }
        });
    }
});

export const clean: Canceler[] = [];

/**
 * 停止所有正在执行的请求
 */
export function stopAllRequest() {
    if (clean.length > 0) {
        for (const canceler of clean) {
            canceler("取消请求");
        }
    }
}

// 每次发起 HTTP 请求时都会首先触发这个拦截器。
const requestFulfilled = (config: InternalAxiosRequestConfig) => {
    if (config.headers.loading === undefined || config.headers.loading === true) {
        showLoading();
        config.headers.loading = undefined;
    }
    const token = useUserStore().token.access_token;
    if (token && !config.url?.includes("/login")) {
        config.headers["Authorization"] = `Bearer ${token}`;
    }
    config.cancelToken = new axios.CancelToken(function executor(c) {
        clean.push(c);
    });
    return config;
};

// 当创建请求或配置请求过程中发生错误时触发。例如，在尝试构造请求对象时发生了异常，或者在请求配置阶段遇到了问题。
const requestRejected = (error: AxiosError) => {
    hideLoading();
    return Promise.reject(error as Error);
};

// HTTP 状态码在 200 ~ 299 之间
const responseFulfilled = (response: AxiosResponse<IResult>) => {
    hideLoading();
    return response;
};

// 网络错误、请求未完成,HTTP 状态码为 4xx、5xx
const responseRejected = (error: AxiosError) => {
    // 无论什么错误，先关闭 loading 并尝试取消所有待定请求
    hideLoading();
    stopAllRequest();

    // 如果是请求被取消（如重复请求触发 cancelToken），直接 reject，不提示
    if (error.name === "CanceledError") {
        return Promise.reject(error);
    }

    const response = error.response;

    // 情况1：服务器有响应（状态码 4xx/5xx 等）
    if (response?.data) {
        const status = response.status;
        const rawData = response.data;
        const msg = isIResult(rawData) ? rawData.msg : "未知错误";

        // 401 认证失败：跳转登录
        if (status === 401) {
            // "认证异常:"
            MessageUtils.error(msg, () => {
                GlobalUtils.toLogin();
            });
            return Promise.resolve(); // 阻止后续 then/catch，不 reject
        }

        // 402 占位处理（可扩展）
        if (status === 402) {
            console.log("预留状态码 402:", msg);
            return Promise.resolve();
        }

        // 其他客户端错误 (400-499)
        if (isStatusCodeInRange(status, 400, 499)) {
            MessageUtils.error(msg);
            return Promise.reject(error);
        }

        // 服务端错误 (500-599)
        if (isStatusCodeInRange(status, 500, 599)) {
            MessageUtils.notify.error(`服务暂时不可用：${msg}`, "服务器错误");
            return Promise.reject(error);
        }

        // 其他状态码（如 3xx 重定向错误等）
        MessageUtils.notify.warning(msg, "请求异常");
        return Promise.reject(error);
    }

    // 情况2：无响应（网络断开、超时、DNS 失败等）
    MessageUtils.notify.error("无法连接到服务器，请检查网络连接", "网络异常");
    return Promise.reject(error);
};

// 请求拦截器
http.interceptors.request.use(requestFulfilled, requestRejected);

// 响应拦截器
http.interceptors.response.use(responseFulfilled, responseRejected);

// 类型守卫：确保 response.data 是 IResult
const isIResult = (data: unknown): data is IResult => {
    return (
        typeof data === "object" &&
        data !== null &&
        "msg" in data &&
        typeof (data as { msg?: unknown }).msg === "string"
    );
};

// 判断状态码是否在指定范围内
const isStatusCodeInRange = (status: number, min: number, max: number): boolean => {
    return status >= min && status <= max;
};

export default http;
