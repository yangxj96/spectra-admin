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

package com.devops00.spectra.oa.meeting.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingPageFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingRecordFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingResponseFrom;
import com.devops00.spectra.oa.meeting.javabean.vo.MeetingVO;

import java.util.UUID;

/**
 * 会仪表-服务
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 11:47
 */
public interface MeetingService extends BaseService<Meeting> {

    /**
     * 创建一个会议
     *
     * @param from 会议标题、起止时间、地点、内容和参会人等创建字段。
     */
    void created(MeetingCreateFrom from);

    /**
     * 分页查询会议
     *
     * @param page   会议列表的页码、页大小及排序字段。
     * @param params 会议标题和会议状态等分页筛选条件。
     * @return 返回按分页条件查询的OA 会议分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<MeetingVO> page(PageFrom page, MeetingPageFrom params);

    /**
     * 响应会议邀请。
     *
     * @param meetingId 待响应会议邀请的会议唯一标识。
     * @param from      参会人对邀请的响应状态。
     */
    void respond(UUID meetingId, MeetingResponseFrom from);

    /**
     * 签到会议。
     *
     * @param meetingId 要登记签到状态的会议唯一标识。
     */
    void checkIn(UUID meetingId);

    /**
     * 保存会议纪要。
     *
     * @param meetingId 要保存纪要的会议唯一标识。
     * @param from      会议结束后的纪要正文。
     */
    void saveRecord(UUID meetingId, MeetingRecordFrom from);
}
