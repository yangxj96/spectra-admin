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

package com.devops00.spectra.notification.service;

import com.devops00.spectra.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.notification.javabean.from.NotificationControlledSendFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationControlledSendApplyVO;
import com.devops00.spectra.notification.javabean.vo.NotificationControlledSendPreviewVO;

/**
 * 通知中心受控发送服务。
 */
public interface NotificationControlledSendService {

    /**
     * 生成短时一次性 Preview。
     */
    NotificationControlledSendPreviewVO preview(NotificationControlledSendFrom params);

    /**
     * 消费 Preview 并通过统一 Gateway 入队。
     */
    NotificationControlledSendApplyVO apply(NotificationControlledSendApplyFrom params);
}
