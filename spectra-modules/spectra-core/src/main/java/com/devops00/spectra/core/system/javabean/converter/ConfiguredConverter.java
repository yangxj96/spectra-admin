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
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;
import com.devops00.spectra.framework.serialization.mapper.GlobalMapperConfig;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
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
    @Mapping(target = "value", expression = "java(exposedValue(source))")
    @Mapping(target = "configured", expression = "java(isSecretConfigured(source))")
    ConfiguredVO toVO(Configured source);

    /**
     * SECRET 配置只返回空值，避免将存储的密码哈希暴露给调用方。
     *
     * @param source 配置实体。
     * @return 普通配置值或秘密配置的空值。
     */
    default String exposedValue(Configured source) {
        return source.getType() == ConfiguredValueType.SECRET ? null : source.getValue();
    }

    /**
     * 判断秘密配置是否已有编码值。
     *
     * @param source 配置实体。
     * @return 存在非空秘密配置时为 true。
     */
    default boolean isSecretConfigured(Configured source) {
        return source.getType() == ConfiguredValueType.SECRET && source.getValue() != null && !source.getValue().isBlank();
    }

    /**
     * 转换到分页的VO信息
     *
     * @param source 分页信息
     * @return IPAGE
     */
    @Mapping(target = "pages", ignore = true)
    Page<ConfiguredVO> toVOPage(Page<Configured> source);
}
