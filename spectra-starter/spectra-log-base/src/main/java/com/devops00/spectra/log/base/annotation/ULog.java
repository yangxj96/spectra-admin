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

package com.devops00.spectra.log.base.annotation;

import com.devops00.spectra.log.base.enums.SysLogType;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.*;

/// 操作日志记录注解
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ULog {

    ///  默认值
    @Language("SpEL")
    String value() default "未填写操作说明";

    /// 日志类型
    SysLogType type() default SysLogType.GENERAL;

}
