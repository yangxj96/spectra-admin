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

package com.devops00.spectra.core.system.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前端可公开读取的系统基础信息。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemPublicConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 系统名称。 */
    private String name;

    /** 系统简称。 */
    private String shortName;

    /** Logo 地址或文件标识。 */
    private String logo;

    /** 系统默认语言。 */
    private String defaultLocale;

    /** 系统默认时区。 */
    private String defaultTimezone;

    /** 是否显示底部版权。 */
    private boolean copyrightEnabled;

    /** 底部版权名称。 */
    private String copyrightName;

    /** 底部版权点击跳转地址。 */
    private String copyrightUrl;
}
