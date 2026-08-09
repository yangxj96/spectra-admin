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

package com.devops00.spectra.oa.notice.javabean.from;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公告创建参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
public class NoticeCreateFrom {

    /**
     * 标题。
     */
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /**
     * 摘要。
     */
    private String summary;

    /**
     * 内容。
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 目标类型字段。
     */
    private String targetType = "ALL";

    /**
     * 目标部门 ID。
     */
    private UUID targetDepartmentId;

    /**
     * 是否要求阅读。
     */
    private Boolean requiredRead = false;

    /**
     * 发布时间。
     */
    private String publishAt;
}
