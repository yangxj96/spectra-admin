/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.core.user.javabean.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 更新当前用户信息入参
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileFrom {

    /// 真实姓名
    private String realName;

    /// 性别
    private Short gender;

    /// 生日
    private String birthday;

    /// 手机号
    private String phone;

    /// 邮箱
    private String email;

    /// 国家
    private String country;

    /// 城市
    private String city;

    /// 语言
    private String language;

    /// 时区
    private String timezone;
}
