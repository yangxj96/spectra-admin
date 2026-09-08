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

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.core.system.javabean.entity.DictItem;

import java.util.List;
import java.util.UUID;

/**
 * 字典(字典数据)业务层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/18 00:00
 */
public interface DictItemService extends BaseService<DictItem> {

    /**
     * 根据字典组ID查询字典数据列表
     *
     * @param gid 需要读取字典项的字典组唯一标识。
     * @return 返回指定字典组下的字典项实体列表；字典组不存在或没有字典项时返回空列表，不返回 null。
     */
    List<DictItem> listByGid(UUID gid);
}
