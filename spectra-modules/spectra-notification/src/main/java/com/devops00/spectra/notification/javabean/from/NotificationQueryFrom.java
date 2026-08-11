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
 * 消息中心分页查询参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Data
public class NotificationQueryFrom {

    /** 消息分类。 */
    private String type;
    /** 是否只查询已读或未读消息。 */
    private Boolean isRead;
    /** 标题或正文关键字。 */
    private String keyword;
    /** 创建时间起点。 */
    private String startTime;
    /** 创建时间终点。 */
    private String endTime;
}
