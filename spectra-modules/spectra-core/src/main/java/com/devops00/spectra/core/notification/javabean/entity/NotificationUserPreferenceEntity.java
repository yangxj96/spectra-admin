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

package com.devops00.spectra.core.notification.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户用途×渠道通知偏好实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_user_preference", schema = "spectra_notification")
public class NotificationUserPreferenceEntity extends BaseEntity {

    /**
     * 用户 ID。
     */
    @TableField("user_id")
    private UUID userId;
    /**
     * 通知用途。
     */
    @TableField("purpose")
    private String purpose;
    /**
     * 通知渠道。
     */
    @TableField("channel")
    private String channel;
    /**
     * 是否启用。
     */
    @TableField("enabled")
    private Boolean enabled;
    /**
     * 是否启用免打扰。
     */
    @TableField("do_not_disturb")
    private Boolean doNotDisturb;
    /**
     * 免打扰开始时间。
     */
    @TableField("do_not_disturb_start")
    private Instant doNotDisturbStart;
    /**
     * 免打扰结束时间。
     */
    @TableField("do_not_disturb_end")
    private Instant doNotDisturbEnd;
}
