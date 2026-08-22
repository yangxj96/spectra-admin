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

package com.devops00.spectra.core.user.imports.javabean.from;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 固定用户导入模板的一行结构化数据。 */
@Data
public class UserImportRowFrom {

    /**
     * 工号由批量导入服务生成，不属于 Excel 或 Preview 请求字段；保留在内部结构中供 Apply 使用。
     */
    @JsonIgnore
    @Size(max = 64, message = "工号不能超过 64 个字符")
    private String employeeNo;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 120, message = "真实姓名不能超过 120 个字符")
    private String realName;

    @NotBlank(message = "手机号码不能为空")
    @Size(max = 40, message = "手机号码不能超过 40 个字符")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱不能超过 255 个字符")
    private String email;

    @NotBlank(message = "部门编码不能为空")
    @Size(max = 80, message = "部门编码不能超过 80 个字符")
    private String departmentCode;

    @NotBlank(message = "语言不能为空")
    @Size(max = 40, message = "语言不能超过 40 个字符")
    private String language;

    @NotBlank(message = "时区不能为空")
    @Size(max = 80, message = "时区不能超过 80 个字符")
    private String timezone;

    @NotBlank(message = "授权方案编码不能为空")
    @Size(max = 80, message = "授权方案编码不能超过 80 个字符")
    private String authorizationProfileCode;
}
