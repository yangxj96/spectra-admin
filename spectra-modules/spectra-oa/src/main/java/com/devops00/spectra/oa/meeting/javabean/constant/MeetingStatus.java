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

package com.devops00.spectra.oa.meeting.javabean.constant;


import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/// 会议状态
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 14:30
@Getter
public enum MeetingStatus implements IEnum<String> {

    DRAFT("draft", "草稿"),
    SCHEDULED("scheduled", "已安排"),
    ONGOING("ongoing", "进行中"),
    FINISHED("finished", "已结束"),
    CANCELLED("cancelled", "已取消");


    /// 状态
    private final String code;

    /// 说明
    private final String name;

    MeetingStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getValue() {
        return this.code;
    }
}
