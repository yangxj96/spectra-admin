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

package com.devops00.spectra.common.notification;

import java.util.UUID;

/** 通知模块使用的收件人快照，不暴露 Core Entity。 */
public record NotificationRecipient(UUID userId, String phone, String email, boolean active, boolean verified) {

    /** 返回指定外部渠道的已验证地址。 */
    public String addressFor(NotificationChannel channel) {
        if (!active || !verified || channel == null) {
            return null;
        }
        return switch (channel) {
            case SMS -> phone;
            case EMAIL -> email;
            case IN_APP -> null;
        };
    }
}
