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

import java.util.List;

import org.mapstruct.Mapper;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationType;
import com.devops00.spectra.oa.application.javabean.from.ApplicationTypeSaveFrom;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationTypeVO;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationVO;

/// OA 申请 MapStruct 转换器。
@Mapper(config = GlobalMapperConfig.class)
public interface ApplicationConverter {
    ApplicationVO toVO(Application source);

    List<ApplicationVO> toVOList(List<Application> source);

    ApplicationTypeVO toTypeVO(ApplicationType source);

    List<ApplicationTypeVO> toTypeVOList(List<ApplicationType> source);

    ApplicationType toTypeEntity(ApplicationTypeSaveFrom source);

    void updateTypeEntity(ApplicationTypeSaveFrom source,
            @org.mapstruct.MappingTarget ApplicationType target);
}
