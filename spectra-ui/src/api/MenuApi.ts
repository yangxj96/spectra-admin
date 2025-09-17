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
    // 获取树形路由
    async tree(): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>("/api/menu/tree").then(res => res.data);
    },
    // 新增菜单
    async created(params: Menu) {
        return http.post<IResult<Menu>>("/api/menu/created", params).then(res => res.data);
    },
    // 修改菜单
    async modify(params: Menu) {
        return http.put<IResult<Menu>>("/api/menu/modify", params).then(res => res.data);
    }
};
