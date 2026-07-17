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

package com.devops00.spectra.oa.attendance.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.attendance.javabean.entity.Attendance;
import com.devops00.spectra.oa.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 考勤主接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:21
@Slf4j
@RestController
@RequestMapping("/oa/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService bindService;

    @ULog("'分页查询考勤'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public IPage<Attendance> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
