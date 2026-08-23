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

package com.devops00.spectra.common.config;

import com.devops00.spectra.common.constant.ConfiguredValueType;

/**
 * 公共运行时配置写入端口；具体存储由平台核心模块提供。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface SystemConfigValueWriter {

    /**
     * 保存或更新一个运行时配置项。
     *
     * @param key     配置键
     * @param value   配置值
     * @param type    配置值类型
     * @param remarks 配置说明
     */
    void upsert(String key, String value, ConfiguredValueType type, String remarks);
}
