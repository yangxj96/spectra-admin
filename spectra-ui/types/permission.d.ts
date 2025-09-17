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

// 权限
type Authority = BaseEntity & {
    // 父ID
    pid?: string;
    // 权限名称
    name: string;
    // 权限编码
    code: string;
};

// 权限树形
type AuthorityTree = Authority & {
    // 下级权限
    children: AuthorityTree[];
};

// 角色
type Role = BaseEntity & {
    //角色名称
    name: string;
    // 角色代码
    code: string;
    //角色状态
    state: boolean;
    //角色范围
    scope: number;
    //角色备注
    remark: string;
};
