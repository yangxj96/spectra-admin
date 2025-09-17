/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import http from "@/plugin/request";

export default {
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     */
    async login(username: string, password: string, code: string) {
        return await http
            .post<IResult<Token>>("/api/auth/login", {
                username: username,
                password: password,
                code: code
            })
            .then(response => response.data);
    },
    // 退出登录
    async logout() {
        return await http.post("/api/auth/logout").then(response => response.data);
    },
    // 检查token是否还能用
    async check() {
        return await http.post("/api/auth/check").then(response => response.data);
    }
};
