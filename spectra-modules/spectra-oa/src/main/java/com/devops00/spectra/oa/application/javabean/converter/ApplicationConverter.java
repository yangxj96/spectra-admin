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

package com.devops00.spectra.oa.application.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationType;
import com.devops00.spectra.oa.application.javabean.from.ApplicationTypeSaveFrom;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationTypeVO;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * OA 申请 MapStruct 转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface ApplicationConverter {
    /**
     * 申请实体转视图对象。
     */
    ApplicationVO toVO(Application source);

    /**
     * 申请实体列表转视图列表。
     */
    List<ApplicationVO> toVOList(List<Application> source);

    /**
     * 申请类型实体转视图对象。
     */
    ApplicationTypeVO toTypeVO(ApplicationType source);

    /**
     * 申请类型实体列表转视图列表。
     */
    List<ApplicationTypeVO> toTypeVOList(List<ApplicationType> source);

    /**
     * 申请类型保存入参转实体。
     */
    ApplicationType toTypeEntity(ApplicationTypeSaveFrom source);

    /**
     * 使用申请类型保存入参更新实体。
     */
    void updateTypeEntity(ApplicationTypeSaveFrom source, @org.mapstruct.MappingTarget ApplicationType target);
}
