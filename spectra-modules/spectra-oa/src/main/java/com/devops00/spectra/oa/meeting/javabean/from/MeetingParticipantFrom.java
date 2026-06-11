package com.devops00.spectra.oa.meeting.javabean.from;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会议-参会人-入参
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/30 15:27
 */
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
