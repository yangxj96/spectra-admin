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
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.from.ConfiguredFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredPageFrom;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;

import java.util.Optional;

/**
 * 系统配置Service层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/6 00:00
 */
public interface ConfiguredService extends BaseService<Configured> {

    /**
     * 修改系统配置的值和说明
     *
     * @param params 待修改配置项的键、值、值类型和备注说明。
     */
    void modify(ConfiguredFrom params);

    /**
     * 分页查询系统配置项
     *
     * @param page   配置项列表的页码、页大小及排序字段。
     * @param params 配置键、配置类型和关键字等筛选条件。
     * @return 返回按配置键、类型等条件分页查询的系统配置项；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params);

    /**
     * 保存或更新配置（按 key 去重）
     *
     * @param key     要保存的稳定配置键，用于后续按键读取和去重。
     * @param value   配置键对应的文本值；具体解析方式由 {@code type} 决定。
     * @param type    配置值的业务类型，用于约束读取方的转换方式。
     * @param remarks 面向管理端展示的配置说明，帮助操作者理解该配置的用途。
     */
    void upsert(String key, String value, ConfiguredValueType type, String remarks);

    /**
     * 查询值。
     *
     * @param key 查询或操作所用的键。
     * @return 可能存在的查询结果。
     */
    Optional<String> findValue(String key);
}
