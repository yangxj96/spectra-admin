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

// 登录token
type Token = {
    id: string;
    // 用户名
    username: string;
    // 认证token
    access_token: string;
    // 权限列表
    authorities: string[];
    // 角色
    roles: Role[];
};

// 用户
type User = BaseEntity & {
    // 名称
    name: string;
    // 邮箱
    email: string;
    // 状态
    state: number;
    // 角色列表
    roles: Role[];
    // 角色ID列表
    role_ids: string[];
    // 组织机构ID
    organization_id: string;
};
