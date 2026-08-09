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

package com.devops00.spectra.oa.calendar.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.calendar.javabean.entity.Calendar;
import com.devops00.spectra.oa.calendar.javabean.from.CalendarSaveFrom;
import com.devops00.spectra.oa.calendar.javabean.vo.CalendarVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 日程对象转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface CalendarConverter {

    /**
     * 日程实体转响应视图。
     */
    CalendarVO toVO(Calendar source);

    /**
     * 日程保存入参转实体，时间由 Service 完成校验后填充。
     */
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    Calendar toEntity(CalendarSaveFrom source);

    /**
     * 使用日程保存入参更新实体，时间由 Service 完成校验后填充。
     */
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    void updateEntity(CalendarSaveFrom source, @MappingTarget Calendar target);
}
