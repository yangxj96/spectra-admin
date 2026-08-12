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

package com.devops00.spectra.oa.notice.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * 公告阅读回执实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_notice_reader", schema = "spectra_oa")
public class NoticeReader extends BaseEntity {

    /**
     * 公告 ID。
     */
    @TableField("notice_id")
    private UUID noticeId;

    /**
     * 用户 ID。
     */
    @TableField("user_id")
    private UUID userId;

    /**
     * 阅读时间。
     */
    @TableField("read_at")
    private Instant readAt;
}
