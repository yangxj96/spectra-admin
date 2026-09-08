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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.from.DictGroupFrom;
import com.devops00.spectra.core.system.javabean.from.DictItemFrom;
import com.devops00.spectra.core.system.javabean.vo.DictGroupTreeVO;
import com.devops00.spectra.core.system.javabean.vo.DictItemVO;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 字典操作业务层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/18 00:00
 */
public interface DictService {

    /**
     * 创建字典组
     *
     * @param params 字典组编码、名称、显示状态和排序等字典组字段。
     */
    void createGroup(DictGroupFrom params);

    /**
     * 根据ID删除字典组
     *
     * @param id 待删除字典组的唯一标识；删除时同时处理其字典项关联。
     */
    void deleteGroup(UUID id);

    /**
     * 修改字典组
     *
     * @param params 待修改字典组的唯一标识及编码、名称、状态和排序字段。
     */
    void modifyGroup(DictGroupFrom params);

    /**
     * 创建字典数据
     *
     * @param params 字典组 ID、字典项编码、名称、值、排序和启用状态等字段。
     */
    void createData(DictItemFrom params);

    /**
     * 根据ID删除字典数据
     *
     * @param id 待删除字典项的唯一标识。
     */
    void deleteData(UUID id);

    /**
     * 修改字典数据
     *
     * @param params 待修改字典项的唯一标识及编码、名称、值、排序和启用状态字段。
     */
    void modifyData(DictItemFrom params);

    /**
     * 获取字典类型列表且转换为树
     *
     * @return 返回字典组及其字典项组装的树；没有字典组时按当前实现返回 null，调用方需要先判空。
     */
    @Nullable
    List<DictGroupTreeVO> listDictGroupWrapTree();

    /**
     * 根据字典类型编码获取字典数据列表
     *
     * @param code 字典类型编码
     * @return 返回指定字典组编码下的字典项视图列表；编码不存在或没有字典项时返回空列表，不返回 null。
     */
    List<DictItemVO> listDictDataByGroupCode(String code);
}
