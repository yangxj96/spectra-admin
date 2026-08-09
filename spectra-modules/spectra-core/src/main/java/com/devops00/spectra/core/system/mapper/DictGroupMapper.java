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

package com.devops00.spectra.core.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典(字典类型)mapper层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/18 00:00
 */
@Mapper
public interface DictGroupMapper extends BaseMapper<DictGroup> {
}
