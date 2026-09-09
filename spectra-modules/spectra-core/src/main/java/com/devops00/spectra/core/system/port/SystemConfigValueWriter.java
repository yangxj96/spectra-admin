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

package com.devops00.spectra.core.system.port;

import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;

/**
 * 公共运行时配置写入端口；具体存储由平台核心模块提供。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface SystemConfigValueWriter {

    /**
     * 保存或更新系统配置表中的一个运行时配置项。
     *
     * @param key     配置项的唯一键；实现通常按该键执行插入或更新
     * @param value   配置项的文本值；具体格式由 {@code type} 约束
     * @param type    配置项的业务类型，用于决定值的解释和校验方式
     * @param remarks 面向管理端的配置说明；允许为空，表示不提供额外说明
     */
    void upsert(String key, String value, ConfiguredValueType type, String remarks);
}
