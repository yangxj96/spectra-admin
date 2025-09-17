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

// 响应整体
type IResult<T = unknown> = {
    // 状态码
    code: number;
    // 消息
    msg: string;
    // 响应内容
    data?: T;
};

// 基础实体都有的类型
type BaseEntity = {
    // 主键ID
    id: string;
    // 创建人
    created_user?: string;
    // 创建时间
    created_time?: string;
    // 最后更新人
    updated_user?: string;
    // 最后更新时间
    updated_time?: string;
};
