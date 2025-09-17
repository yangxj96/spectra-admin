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
import type { TreeKey } from "element-plus";

export default {
    // 创建角色
    async createdRole(params: Role) {
        return http.post("/api/permission/role", params).then(res => res.data);
    },
    // 修改角色
    async modifyRole(params: Role) {
        return http.put("/api/permission/role", params).then(res => res.data);
    },
    // 分页查询
    async pageRole(params?: RolePageParams): Promise<IResult<Page<Role>>> {
        return http
            .get<IResult<Page<Role>>>("/api/permission/role/page", {
                params
            })
            .then(res => res.data);
    },
    // 列表查询
    async listRole(): Promise<IResult<Role[]>> {
        return http.get<IResult<Role[]>>("/api/permission/role/list").then(res => res.data);
    },
    // 权限树查询
    async authorityTree(): Promise<IResult<AuthorityTree[]>> {
        return http.get<IResult<AuthorityTree[]>>("/api/permission/authority/tree").then(res => res.data);
    },
    // 获取当前角色下有哪些权限
    async getRoleAuthority(roleId: string): Promise<IResult<Authority[]>> {
        return http.get<IResult<Authority[]>>(`/api/permission/role/${roleId}/authority`).then(res => res.data);
    },
    // 获取当前角色下有哪些菜单
    async getRoleMenu(roleId: string): Promise<IResult<Menu[]>> {
        return http.get<IResult<Menu[]>>(`/api/permission/role/${roleId}/menu`).then(res => res.data);
    },
    // 获取当前角色下有哪些权限
    async saveRoleAuthority(params: { role_id: string; authority_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.post<IResult>(`/api/permission/role/${params.role_id}/authority`, params).then(res => res.data);
    },
    // 获取当前角色下有哪些菜单
    async saveRoleMenu(params: { role_id: string; menu_ids: TreeKey[] | undefined }): Promise<IResult> {
        return http.post<IResult>(`/api/permission/role/${params.role_id}/menu`, params).then(res => res.data);
    }
};
