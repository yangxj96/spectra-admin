import axios, { type AxiosError, type AxiosResponse, type Canceler, type InternalAxiosRequestConfig } from "axios";
import { hideLoading, showLoading } from "@/plugin/element/loading";
import { ElMessage } from "element-plus/es";
import useUserStore from "@/plugin/store/modules/useUserStore";
import GlobalUtils from "@/utils/GlobalUtils.ts";
import qs from "qs";

// 常见内容类型
// application/x-www-form-urlencoded
// application/json

const http = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 60 * 1000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json"
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
    if (config.headers.loading == undefined || config.headers.loading === true) {
        showLoading();
        config.headers.loading = undefined;
    }
    const token = useUserStore().token.access_token;
    if (token != undefined || token != "") {
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
    hideLoading();
    if (error.name === "CanceledError") {
        return Promise.reject(error);
    }
    if (error.response?.data as IResult) {
        let result = error.response?.data as IResult;
        switch (error.response?.status) {
            case 401: {
                ElMessage.error({
                    message: result.msg,
                    onClose: () => {
                        GlobalUtils.exit();
                    }
                });
                return;
            }
            case 402: {
                console.log("占位用一下");
                return;
            }
            default: {
                ElMessage.error((error.response?.data as IResult).msg);
            }
        }
    } else {
        ElMessage.error({
            type: "error",
            message: "网络错误,请检查网络"
        });
    }
    return Promise.reject(error);
};

// 请求拦截器
http.interceptors.request.use(requestFulfilled, requestRejected);

// 响应拦截器
http.interceptors.response.use(responseFulfilled, responseRejected);

export default http;
