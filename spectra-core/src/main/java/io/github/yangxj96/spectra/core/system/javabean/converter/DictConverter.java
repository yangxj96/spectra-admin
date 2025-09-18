/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.system.javabean.converter;

import io.github.yangxj96.spectra.core.system.javabean.entity.DictData;
import io.github.yangxj96.spectra.core.system.javabean.entity.DictGroup;
import io.github.yangxj96.spectra.core.system.javabean.from.DictDataFrom;
import io.github.yangxj96.spectra.core.system.javabean.from.DictGroupFrom;
import io.github.yangxj96.spectra.core.system.javabean.vo.DictDataVo;
import io.github.yangxj96.spectra.core.system.javabean.vo.DictTypeTreeVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * <p>
 * 字典Mapstruct
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/18
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictConverter {

    /**
     * 字典类型入参转实体
     *
     * @param from 字典类型入参
     * @return 转换后的实体
     */
    DictGroup groupFromToEntity(DictGroupFrom from);

    /**
     * 字典数据入参转实体
     *
     * @param from 字典数据入参
     * @return 转换后的实体
     */
    DictData dataFromToEntity(DictDataFrom from);

    /**
     * 字典类型转字典树类型
     *
     * @param datum 字典类型
     * @return 字典类型
     */
    DictTypeTreeVO typeToTreeVO(DictGroup datum);

    /**
     * 字典类型列表转字典类型树列表
     *
     * @param datum 字典类型列表
     * @return 字典类型树列表
     */
    List<DictTypeTreeVO> typeToTreeVOS(List<DictGroup> datum);

    /**
     * 字典数据转字典数据VO
     *
     * @param list 字典数据
     * @return 字典数据VO
     */
    DictDataVo dataToVos(DictData list);

    /**
     * 字典数据列表转字典数据VO列表
     *
     * @param list 字典数据列表
     * @return 字典数据VO列表
     */
    List<DictDataVo> dataToVos(List<DictData> list);
}
