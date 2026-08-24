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

package com.devops00.spectra.oa.notice.javabean.constant;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 公告发布状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum NoticeStatus implements IEnum<String> {

    /** 草稿。 */
    DRAFT("DRAFT"),
    /** 定时发布。 */
    SCHEDULED("SCHEDULED"),
    /** 已发布。 */
    PUBLISHED("PUBLISHED"),
    /** 已撤回。 */
    REVOKED("REVOKED");

    private final String value;

    NoticeStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
