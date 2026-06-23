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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.from.ConfiguredFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredPageFrom;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;

/// 系统配置Service层
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/6 00:00
public interface ConfiguredService extends BaseService<Configured> {

    /// 修改系统配置的值和说明
    ///
    /// @param params 修改入参
    void modify(ConfiguredFrom params);


    /// 分页查询系统配置项
    ///
    /// @param page   分页信息
    /// @param params 过滤参数
    /// @return 分页响应信息
    IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params);
}
