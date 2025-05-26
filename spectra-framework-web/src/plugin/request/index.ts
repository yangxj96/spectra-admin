import axios, { type AxiosError, type AxiosResponse, type Canceler, type InternalAxiosRequestConfig } from "axios";
import { hideLoading, showLoading } from "@/plugin/element/loading";
import { ElMessage } from "element-plus/es";
import useUserStore from "@/plugin/store/modules/useUserStore";

const http = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 60 * 1000,
    withCredentials: false,
    headers: {
        "Content-Type": "application/json"
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

/**
 * 请求拦截器 配置请求
 * @param config 相关配置
 */
const requestFulfilled = (config: InternalAxiosRequestConfig) => {
    if (config.headers.loading == undefined || config.headers.loading === true) {
        showLoading();
        config.headers.loading = undefined;
    }
    const token = useUserStore().token.access_token;
    if (token != undefined || token != "") {
        config.headers["Authorization"] = token;
    }
    config.cancelToken = new axios.CancelToken(function executor(c) {
        clean.push(c);
    });
    return config;
};

/**
 * 请求失败情况,在请求之前就失败了
 * @param error
 */
const requestRejected = (error: AxiosError) => {
    hideLoading();
    return Promise.reject(error as Error);
};

/**
 * 请求拦截
 * 在2xx范围内的任何状态代码都会触发此函数，这里主要用于处理响应数据
 * @param response
 */
const responseFulfilled = (response: AxiosResponse<IResult>) => {
    hideLoading();
    if (response.data.code != 0) {
        ElMessage.error({
            type: "error",
            message: response.data.msg
        });
        throw new Error(`请求失败:${response.data.msg}`);
    }
    return response;
};

/**
 * 主要用于发送请求就直接出错了,比如网络错误之类的
 * @param error
 */
const responseRejected = (error: AxiosError) => {
    console.log(error);
    hideLoading();
    if (error.name === "CanceledError") {
        return Promise.reject(error);
    }
    if (error.response) {
        switch (error.response.status) {
            case 404: {
                ElMessage.error({
                    type: "error",
                    message: "请求URL错误"
                });
                break;
            }
            case 500: {
                ElMessage.error({
                    type: "error",
                    message: "服务器异常"
                });
                break;
            }
            default: {
                ElMessage.error({
                    type: "error",
                    message: "请求失败,请稍后重试"
                });
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
