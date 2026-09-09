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

package com.devops00.spectra.core.quartz.javabean.vo;

import com.devops00.spectra.common.port.quartz.QuartzParameterSchema;
import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;

import java.util.List;
import java.util.Map;

/** 代码白名单中的 Quartz Job 类型能力描述。 */
public record QuartzJobTypeVO(String typeKey, String displayName, boolean protectedJob,
                              String jobClassName, String parameterVersion,
                              Map<String, QuartzParameterSchema.FieldDefinition> parameterFields,
                              boolean allowUnknownParameters,
                              List<QuartzTriggerTemplate.TriggerType> supportedTriggerTypes) {

    /** 规范化集合字段，保证响应不会返回可变空值。 */
    public QuartzJobTypeVO {
        parameterFields = parameterFields == null ? Map.of() : Map.copyOf(parameterFields);
        supportedTriggerTypes = supportedTriggerTypes == null ? List.of() : List.copyOf(supportedTriggerTypes);
    }
}
