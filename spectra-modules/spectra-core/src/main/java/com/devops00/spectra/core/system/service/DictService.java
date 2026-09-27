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
import com.devops00.spectra.core.system.javabean.from.DictItemDefaultFrom;
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
     * @param id 待删除字典组的唯一标识；包含字典项的字典组不能删除。
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
     * 修改字典数据
     *
     * @param params 待修改字典项的标签、排序和启用状态等字段；字典组和值创建后不可变。
     */
    void modifyData(DictItemFrom params);

    /**
     * 启用字典项。
     *
     * @param id 对应字典项的唯一标识
     */
    void enableData(UUID id);

    /**
     * 禁用字典项并保留其历史引用。
     *
     * @param id 字典项唯一标识
     */
    void disableData(UUID id);

    /**
     * 设置或取消字典项默认状态。
     *
     * @param id     对应字典项的唯一标识
     * @param params 默认状态
     */
    void setDataDefault(UUID id, DictItemDefaultFrom params);

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
