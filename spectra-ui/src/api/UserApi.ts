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

import http from "@/plugin/request/index.ts";

export default {
    // 分页获取用户列表
    async page(params?: UserPageParams): Promise<IResult<Page<User>>> {
        return http.get<IResult<Page<User>>>("/api/user/page", { params }).then(res => res.data);
    },
    // 新增用户
    async created(params: User) {
        return http.post<IResult>("/api/user", params).then(res => res.data);
    },
    // 修改用户
    async modify(params: User) {
        return http.put<IResult>("/api/user", params).then(res => res.data);
    },
    // 修改用户
    async deleteById(id: string) {
        return http.delete<IResult>(`/api/user/${id}`).then(res => res.data);
    },
    // 重置用户密码
    async passwordResetById(id: string) {
        return http.put<IResult>(`/api/user/password/reset/${id}`).then(res => res.data);
    }
};
