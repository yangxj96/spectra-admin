/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.core.service;

import com.yangxj96.spectra.service.core.javabean.from.DictDataFrom;
import com.yangxj96.spectra.service.core.javabean.from.DictGroupFrom;
import com.yangxj96.spectra.service.core.javabean.vo.DictDataVo;
import com.yangxj96.spectra.service.core.javabean.vo.DictTypeTreeVO;

import java.util.List;

/**
 * <p>
 * 字典操作业务层
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/18
 */
public interface DictService {

    /**
     * 创建字典组
     *
     * @param params 入参from
     */
    void createGroup(DictGroupFrom params);

    /**
     * 修改字典组
     * @param params 入参from
     */
    void modifyGroup(DictGroupFrom params);

    /**
     * 创建字典数据
     *
     * @param params 入参from
     */
    void createData(DictDataFrom params);

    /**
     * 修改字典数据
     *
     * @param params 入参from
     */
    void modifyData(DictDataFrom params);

    /**
     * 获取字典类型列表且转换为树
     *
     * @return 字典类型树
     */
    List<DictTypeTreeVO> listDictGroupWrapTree();

    /**
     * 根据字典类型编码获取字典数据列表
     *
     * @param code 字典类型编码
     * @return 字典数据列表
     */
    List<DictDataVo> listDictDataByGroupCode(String code);

}
