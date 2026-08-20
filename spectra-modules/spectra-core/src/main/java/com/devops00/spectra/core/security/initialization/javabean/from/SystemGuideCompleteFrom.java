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

package com.devops00.spectra.core.security.initialization.javabean.from;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * 系统设置引导完成参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
@Data
public class SystemGuideCompleteFrom {

    /** 初始化根部门名称。 */
    @NotBlank(message = "根部门名称不能为空")
    @Size(max = 100, message = "根部门名称长度不能超过 100 个字符")
    private String rootDepartmentName;

    /** 初始化根部门所属区域。 */
    @NotNull(message = "根部门所属区域不能为空")
    private UUID rootDepartmentRegionId;

    /** 初始化根部门类型，对应字典组 sys_organization_type。 */
    @NotNull(message = "根部门类型不能为空")
    private Short rootDepartmentType;

    /** 是否启用接口请求加解密。 */
    @NotNull(message = "是否启用接口加解密不能为空")
    private Boolean cryptoEnabled;

    /** 是否启用统一通知模块。 */
    @NotNull(message = "是否启用通知模块不能为空")
    private Boolean notificationEnabled;

    /** 是否显示底部版权。 */
    @NotNull(message = "是否启用底部版权不能为空")
    private Boolean copyrightEnabled;

    /** 底部版权名称。 */
    @Size(max = 100, message = "底部版权名称长度不能超过 100 个字符")
    private String copyrightName;

    /** 底部版权点击跳转地址。 */
    @Size(max = 500, message = "底部版权跳转地址长度不能超过 500 个字符")
    private String copyrightUrl;
}
