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

package com.devops00.spectra.oa.attendance.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.attendance.javabean.entity.Attendance;
import com.devops00.spectra.oa.attendance.mapper.AttendanceMapper;
import com.devops00.spectra.oa.attendance.service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 考勤表主表-服务默认实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 14:05
@Slf4j
@Service
public class AttendanceServiceImpl extends BaseServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {
}
