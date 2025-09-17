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

// 手机号码验证规则
import type { FormItemRule } from "element-plus";

export const mobile: FormItemRule["validator"] = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入手机号"));
    }
    const reg = /^(13[0-9]|14[01456879]|15[0-3,5-9]|16[2567]|17[0-8]|18[0-9]|19[0-3,5-9])\d{8}$/; // 简单的中国大陆手机号正则表达式
    if (reg.test(value)) {
        callback();
    } else {
        callback(new Error("请输入有效的手机号"));
    }
};

export const email: FormItemRule["validator"] = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入邮箱地址"));
    }
    const reg = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
    if (reg.test(value)) {
        callback();
    } else {
        callback(new Error("请输入有效的邮箱地址"));
    }
};
