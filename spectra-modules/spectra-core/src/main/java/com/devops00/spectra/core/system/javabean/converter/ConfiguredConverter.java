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

package com.devops00.spectra.core.system.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 系统配置Mapstruct
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/6 00:00
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface ConfiguredConverter {

    /**
     * 数据库实体转VO
     *
     * @param source 数据库实体
     * @return VO
     */
    ConfiguredVO toVO(Configured source);

    /**
     * 转换到分页的VO信息
     *
     * @param source 分页信息
     * @return IPAGE
     */
    @Mapping(target = "pages", ignore = true)
    Page<ConfiguredVO> toVOPage(Page<Configured> source);
}
