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

package com.devops00.spectra.core.notification.javabean.vo;

import lombok.Data;

import java.util.UUID;

/**
 * 受控发送使用的模板版本和脱敏渲染样例。
 */
@Data
public class NotificationControlledSendTemplateVO {

    private UUID templateId;

    private String channel;

    private Integer versionNo;

    private String versionDigest;

    private String title;

    private String content;

    private String html;
}
