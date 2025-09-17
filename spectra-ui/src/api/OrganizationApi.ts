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
    // 获取组织机构树形列表
    async tree() {
        return await http.get<IResult<OrganizationTree[]>>("/api/organization/tree").then(res => res.data);
    },
    // 新增组织机构
    async created(params: Organization): Promise<IResult> {
        return await http.post<IResult>("/api/organization", params).then(res => res.data);
    },
    // 根据ID删除组织机构
    async deleteById(id: string): Promise<IResult> {
        return await http.delete<IResult>(`/api/organization/${id}`).then(res => res.data);
    },
    // 修改组织机构
    async modify(params: Organization): Promise<IResult> {
        return http.put<IResult>("/api/organization", params).then(res => res.data);
    }
};
