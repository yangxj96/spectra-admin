package com.devops00.spectra.oa.attendance.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.attendance.javabean.entity.Attendance;
import com.devops00.spectra.oa.attendance.mapper.AttendanceMapper;
import com.devops00.spectra.oa.attendance.service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 考勤表主表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 14:05
@Slf4j
@Service
public class AttendanceServiceImpl extends BaseServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {
}
