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

package com.devops00.spectra.oa.meeting.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingPageFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingRecordFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingResponseFrom;
import com.devops00.spectra.oa.meeting.javabean.vo.MeetingVO;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 会议主接口
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/5 23:23
 */
@Slf4j
@RestController
@RequestMapping("/oa/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService bindService;

    /**
     * 创建一个会议
     */
    @ULog("'创建会议'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:meeting:create')")
    public void created(@Validated(Verify.Insert.class) @RequestBody MeetingCreateFrom from) {
        bindService.created(from);
    }

    /**
     * 分页查询会议
     */
    @ULog("'分页查询会议'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:meeting:read')")
    public IPage<MeetingVO> page(PageFrom page, MeetingPageFrom params) {
        return bindService.page(page, params);
    }

    /**
     * 响应会议邀请。
     */
    @ULog("'响应会议邀请'")
    @PostMapping(value = "/{id}/response", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:meeting:update')")
    public void respond(@PathVariable UUID id, @Validated @RequestBody MeetingResponseFrom from) {
        bindService.respond(id, from);
    }

    /**
     * 会议签到。
     */
    @ULog("'会议签到'")
    @PostMapping(value = "/{id}/check-in", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:meeting:update')")
    public void checkIn(@PathVariable UUID id) {
        bindService.checkIn(id);
    }

    /**
     * 保存会议纪要。
     */
    @ULog("'保存会议纪要'")
    @PostMapping(value = "/{id}/record", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:meeting:update')")
    public void saveRecord(@PathVariable UUID id, @Validated @RequestBody MeetingRecordFrom from) {
        bindService.saveRecord(id, from);
    }
}
