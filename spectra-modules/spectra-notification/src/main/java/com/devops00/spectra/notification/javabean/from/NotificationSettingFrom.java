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

package com.devops00.spectra.notification.javabean.from;

import lombok.Data;

/**
 * 旧消息中心设置兼容入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationSettingFrom {

    /** 是否接收系统通知。 */
    private Boolean systemEnabled;
    /** 是否接收流程通知。 */
    private Boolean workflowEnabled;
    /** 是否接收 OA 通知。 */
    private Boolean oaEnabled;
    /** 是否接收站内私信。 */
    private Boolean innerMailEnabled;
    /** 是否接收审批通知。 */
    private Boolean approvalEnabled;
    /** 是否开启免打扰。 */
    private Boolean doNotDisturb;
    /** 免打扰开始时间。 */
    private String doNotDisturbStart;
    /** 免打扰结束时间。 */
    private String doNotDisturbEnd;
}
