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

package com.devops00.spectra.oa.calendar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.calendar.javabean.entity.Calendar;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarPageFrom;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarSaveFrom;
import com.devops00.spectra.oa.calendar.javabean.vo.CalendarVO;

import java.util.UUID;

/**
 * 日程业务服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
public interface CalendarService extends BaseService<Calendar> {
    /**
     * 分页查询日程。
     */
    IPage<CalendarVO> page(PageFrom page, CalendarPageFrom params);

    /**
     * 查询日程详情。
     */
    CalendarVO get(UUID id);

    /**
     * 创建日程。
     */
    CalendarVO create(CalendarSaveFrom from);

    /**
     * 修改日程。
     */
    CalendarVO update(UUID id, CalendarSaveFrom from);

    /**
     * 删除日程。
     */
    void delete(UUID id);
}
