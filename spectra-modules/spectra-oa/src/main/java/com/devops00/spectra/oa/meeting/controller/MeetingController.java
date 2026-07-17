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
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingPageFrom;
import com.devops00.spectra.oa.meeting.javabean.vo.MeetingVO;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/// 会议主接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:23
@RestController
@RequestMapping("/oa/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService bindService;

    /// 创建一个会议
    @ULog("'创建会议'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_MEETING:INSERT')")
    public void create(@RequestBody MeetingCreateFrom from) {
        bindService.create(from);
    }

    /// 分页查询会议
    @ULog("'分页查询会议'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public IPage<MeetingVO> page(PageFrom page, MeetingPageFrom params) {
        return bindService.page(page, params);
    }


}
