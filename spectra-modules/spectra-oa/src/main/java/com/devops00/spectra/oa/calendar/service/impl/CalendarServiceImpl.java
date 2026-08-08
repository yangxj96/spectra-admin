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

package com.devops00.spectra.oa.calendar.service.impl;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.calendar.javabean.converter.CalendarConverter;
import com.devops00.spectra.oa.calendar.javabean.entity.Calendar;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarPageFrom;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarSaveFrom;
import com.devops00.spectra.oa.calendar.javabean.vo.CalendarVO;
import com.devops00.spectra.oa.calendar.mapper.CalendarMapper;
import com.devops00.spectra.oa.calendar.service.CalendarService;
import com.devops00.spectra.security.base.holder.SecUtil;

import lombok.RequiredArgsConstructor;

/// 日程业务服务实现。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl extends BaseServiceImpl<CalendarMapper, Calendar> implements CalendarService {

    private final CalendarConverter calendarConverter;
    private final TimeMapper timeMapper;

    @Override
    public IPage<CalendarVO> page(PageFrom page, CalendarPageFrom params) {
        var user = SecUtil.getCurrentUser();
        var userId = SecUtil.getCurrentUserId();
        if (user == null || userId == null) {
            return new Page<>(page.getPageNum(), page.getPageSize());
        }
        var wrapper = new LambdaQueryWrapper<Calendar>()
                .and(q -> q.eq(Calendar::getOwnerId, userId)
                        .or().eq(Calendar::getVisibility, "ALL")
                        .or(w -> w.eq(Calendar::getVisibility, "DEPARTMENT")
                                .eq(Calendar::getDepartmentId, user.getDepartmentId())))
                .orderByAsc(Calendar::getStartTime);
        if (StringUtils.hasText(params.getKeyword())) {
            wrapper.like(Calendar::getTitle, params.getKeyword());
        }
        if (StringUtils.hasText(params.getStartTime())) {
            wrapper.ge(Calendar::getEndTime, parseTime(params.getStartTime()));
        }
        if (StringUtils.hasText(params.getEndTime())) {
            wrapper.le(Calendar::getStartTime, parseTime(params.getEndTime()));
        }
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<CalendarVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(calendarConverter::toVO).toList());
        return voPage;
    }

    @Override
    public CalendarVO get(UUID id) {
        var calendar = require(id);
        ensureOwner(calendar);
        return calendarConverter.toVO(calendar);
    }

    @Override
    @Transactional
    public CalendarVO create(CalendarSaveFrom from) {
        var user = SecUtil.getCurrentUser();
        var userId = SecUtil.getCurrentUserId();
        if (user == null || userId == null || user.getDepartmentId() == null) {
            throw new DataSaveException("当前用户组织信息不可用");
        }
        var start = parseTime(from.getStartTime());
        var end = parseTime(from.getEndTime());
        if (!end.isAfter(start)) {
            throw new DataSaveException("日程结束时间必须晚于开始时间");
        }
        var calendar = calendarConverter.toEntity(from);
        calendar.setOwnerId(userId);
        calendar.setDepartmentId(user.getDepartmentId());
        calendar.setStartTime(start);
        calendar.setEndTime(end);
        calendar.setAllDay(Boolean.TRUE.equals(from.getAllDay()));
        calendar.setEventType(StringUtils.hasText(from.getEventType()) ? from.getEventType() : "PERSONAL");
        calendar.setVisibility(StringUtils.hasText(from.getVisibility()) ? from.getVisibility() : "PRIVATE");
        if (!this.save(calendar)) {
            throw new DataSaveException("保存日程失败");
        }
        return calendarConverter.toVO(calendar);
    }

    @Override
    @Transactional
    public CalendarVO update(UUID id, CalendarSaveFrom from) {
        var calendar = require(id);
        ensureOwner(calendar);
        var start = parseTime(from.getStartTime());
        var end = parseTime(from.getEndTime());
        if (!end.isAfter(start)) {
            throw new DataSaveException("日程结束时间必须晚于开始时间");
        }
        calendarConverter.updateEntity(from, calendar);
        calendar.setStartTime(start);
        calendar.setEndTime(end);
        calendar.setAllDay(Boolean.TRUE.equals(from.getAllDay()));
        calendar.setEventType(StringUtils.hasText(from.getEventType()) ? from.getEventType() : "PERSONAL");
        calendar.setVisibility(StringUtils.hasText(from.getVisibility()) ? from.getVisibility() : "PRIVATE");
        if (!this.updateById(calendar)) {
            throw new DataSaveException("更新日程失败");
        }
        return calendarConverter.toVO(calendar);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var calendar = require(id);
        ensureOwner(calendar);
        if (!this.removeById(id)) {
            throw new DataSaveException("删除日程失败");
        }
    }

    private Calendar require(UUID id) {
        var calendar = this.getById(id);
        if (calendar == null) {
            throw new DataNotExistException("日程不存在");
        }
        return calendar;
    }

    private void ensureOwner(Calendar calendar) {
        if (!Objects.equals(calendar.getOwnerId(), SecUtil.getCurrentUserId())) {
            throw new DataNotExistException("日程不存在或无权访问");
        }
    }

    private Instant parseTime(String value) {
        if (!StringUtils.hasText(value)) {
            throw new DataSaveException("日程时间不能为空");
        }
        try {
            return timeMapper.toInstant(value);
        } catch (RuntimeException exception) {
            throw new DataSaveException("日程时间格式不正确");
        }
    }
}
