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

/// 会议-参会人-入参
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 15:27
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingParticipantFrom {

    /// 会议ID
    private String meetingId;

    /// 参会人ID
    private String userId;

    /// 角色
    ///
    /// |值|说明|
    /// |----|----|
    /// |host|主持人|
    /// |attendee|参会人|
    /// |optional|可选参会人|
    private String role;
}
