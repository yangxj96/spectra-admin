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
 * 通知模板分页查询入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class NotificationTemplatePageFrom {

    /**
     * 逻辑模板组编码。
     */
    private String templateGroupCode;

    /**
     * 通知渠道。
     */
    private String channel;

    /**
     * 通知用途。
     */
    private String purpose;

    /**
     * 生命周期状态。
     */
    private String state;
}
