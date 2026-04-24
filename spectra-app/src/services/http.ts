import type { RequestMethod, RequestOptions, RequestResult } from "@/types";

const BASE_URL = "https://jsonplaceholder.typicode.com";

// 封装 request
export function request<T>(options: RequestOptions): Promise<RequestResult<any>> {
    return new Promise((resolve, reject) => {
        uni.request({
            url: BASE_URL + options.url,
            method: options.method ?? ("GET" as RequestMethod),
            data: options.data ?? {},
            header: buildHeader(options.header),
            timeout: options.timeout ?? 15000,

            success(res) {
                console.log(res);
                resolve({
                    code: 0,
                    msg: "测试",
                    data: res.data
                });

                // const data = res.data as UTSJSONObject

                // const code = data["code"] as number

                // if (code == 0) {
                // 	resolve({
                // 		code: 0,
                // 		data: data["data"] as T,
                // 		msg: data["msg"] as string
                // 	})
                // 	return
                // }

                // reject({
                // 	code,
                // 	msg: data["msg"] as string
                // })
            },

            fail(err) {
                reject({
                    code: -1,
                    msg: err.errMsg
                });
            }
        });
    });
}

/**
 * 获取token
 */
function getToken(): string | null {
    return uni.getStorageSync("token") as string;
}

/**
 * 构建请求头
 */
function buildHeader(custom?: Record<string, string>): Record<string, string> {
    const header = custom ?? {};

    const token = getToken();
    if (token != null && token != "") {
        header["Authorization"] = "Bearer " + token;
    }

    header["Content-Type"] = "application/json";

    return header;
}
