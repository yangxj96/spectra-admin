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

package com.devops00.spectra.oa.meeting.javabean.vo;

import com.devops00.spectra.oa.meeting.javabean.constant.MeetingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 会议列表VO
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 会议标题
     */
    private String title;

    /**
     * 发起人ID
     */
    private String initiatorId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 会议地点
     */
    private String location;

    /**
     * 会议内容/议题
     */
    private String content;

    /**
     * 会议业务状态
     */
    private MeetingStatus status;

    /**
     * 工作流审核状态
     */
    private MeetingStatus approvalStatus;

    /**
     * 工作流审核实例ID
     */
    private String processInstanceId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
