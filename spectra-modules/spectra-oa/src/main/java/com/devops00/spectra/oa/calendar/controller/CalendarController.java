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

package com.devops00.spectra.oa.calendar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarPageFrom;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarSaveFrom;
import com.devops00.spectra.oa.calendar.javabean.vo.CalendarVO;
import com.devops00.spectra.oa.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 日程协同接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@RestController
@RequestMapping("/oa/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 查询日程列表。
     */
    @ULog("'查询日程列表'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:calendar:read')")
    public IPage<CalendarVO> page(PageFrom page, CalendarPageFrom params) {
        return calendarService.page(page, params);
    }

    /**
     * 获取日程详情。
     */
    @ULog("'获取日程详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:calendar:read')")
    public CalendarVO get(@PathVariable UUID id) {
        return calendarService.get(id);
    }

    /**
     * 创建日程。
     */
    @ULog("'创建日程'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:calendar:create')")
    public CalendarVO create(@Validated(Verify.Insert.class) @RequestBody CalendarSaveFrom from) {
        return calendarService.create(from);
    }

    /**
     * 更新日程。
     */
    @ULog("'更新日程'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:calendar:update')")
    public CalendarVO update(@PathVariable UUID id, @Validated(Verify.Update.class) @RequestBody CalendarSaveFrom from) {
        return calendarService.update(id, from);
    }

    /**
     * 删除日程。
     */
    @ULog("'删除日程'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:calendar:delete')")
    public void delete(@PathVariable UUID id) {
        calendarService.delete(id);
    }
}
