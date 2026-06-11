package com.devops00.spectra.oa.calendar.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.calendar.javabean.entity.Calendar;
import com.devops00.spectra.oa.calendar.mapper.CalendarMapper;
import com.devops00.spectra.oa.calendar.service.CalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 日历表主表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 14:01
@Slf4j
@Service
public class CalendarServiceImpl extends BaseServiceImpl<CalendarMapper, Calendar> implements CalendarService {
}
