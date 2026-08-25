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

package com.devops00.spectra.notification.javabean.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知模板组管理视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/25
 */
@Data
public class NotificationTemplateGroupVO {

    /**
     * 逻辑模板组编码。
     */
    private String templateGroupCode;

    /**
     * 模板名称。
     */
    private String templateName;

    /**
     * 通知用途。
     */
    private String purpose;

    /**
     * 该模板组下的渠道版本。
     */
    private List<NotificationTemplateChannelGroupVO> channels;

    /**
     * 模板组中最近一次变更时间。
     */
    private LocalDateTime updatedAt;
}
