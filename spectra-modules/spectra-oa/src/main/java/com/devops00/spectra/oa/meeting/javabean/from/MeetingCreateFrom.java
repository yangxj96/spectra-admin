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

package com.devops00.spectra.oa.meeting.javabean.from;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/// 会议创建
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 15:24
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreateFrom {

    /// 会议标题
    private String title;

    /// 发起人
    private String initiatorId;

    /// 开始时间
    private String startTime;

    /// 结束时间
    private String endTime;

    /// 会议地点
    private String location;

    /// 会议内容/议题
    private String content;

    /// 参会人员列表
    private List<MeetingParticipantFrom> participants;
}
