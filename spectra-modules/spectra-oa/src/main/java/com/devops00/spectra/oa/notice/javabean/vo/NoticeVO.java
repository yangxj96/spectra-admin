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

package com.devops00.spectra.oa.notice.javabean.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 公告响应视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
public class NoticeVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 标题。
     */
    private String title;

    /**
     * 摘要。
     */
    private String summary;

    /**
     * 内容。
     */
    private String content;

    /**
     * 状态。
     */
    private String status;

    /**
     * 目标类型字段。
     */
    private String targetType;

    /**
     * 目标部门 ID。
     */
    private UUID targetDepartmentId;

    /**
     * 发布人 ID。
     */
    private UUID publisherId;

    /**
     * 发布时间。
     */
    private LocalDateTime publishAt;

    /**
     * 是否要求阅读。
     */
    private Boolean requiredRead;

    /**
     * 是否已读。
     */
    private Boolean read;

    /**
     * 阅读时间。
     */
    private LocalDateTime readAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
