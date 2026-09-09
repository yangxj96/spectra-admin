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
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
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
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 日程关键字和起止时间等分页筛选条件。
     * @return 返回按分页条件查询的OA 日历事件分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<CalendarVO> page(PageFrom page, CalendarPageFrom params);

    /**
     * 查询日程详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 日历事件详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    CalendarVO get(UUID id);

    /**
     * 创建日程。
     *
     * @param from 日程标题、内容、起止时间、全天标记、类型、可见范围和参与人等创建字段。
     * @return 返回保存后的OA 日历事件；校验或写入失败时抛出业务异常，不返回 null。
     */
    CalendarVO create(CalendarSaveFrom from);

    /**
     * 修改日程。
     *
     * @param id   待修改日程的唯一标识。
     * @param from 日程标题、内容、起止时间、全天标记、类型、可见范围和参与人等修改字段。
     * @return 返回保存后的OA 日历事件；校验或写入失败时抛出业务异常，不返回 null。
     */
    CalendarVO update(UUID id, CalendarSaveFrom from);

    /**
     * 删除日程。
     *
     * @param id 待删除日程的唯一标识。
     */
    void delete(UUID id);
}
