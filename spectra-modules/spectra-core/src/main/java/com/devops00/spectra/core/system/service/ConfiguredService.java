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

import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchFrom;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;

import java.util.Optional;
import java.util.List;

/**
 * 系统配置Service层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/6 00:00
 */
public interface ConfiguredService extends BaseService<Configured> {

    /**
     * 查询按业务分类组织的全部配置表单项。
     *
     * @return 配置表单项。
     */
    List<ConfiguredVO> settings();

    /**
     * 在单个事务中保存一个配置业务分类的完整表单。
     *
     * @param params 分类及完整配置项。
     */
    void modifyBatch(ConfiguredBatchFrom params);

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
     * 保存或更新需要通过字典组呈现的配置。
     *
     * @param key      稳定配置键。
     * @param value    配置值。
     * @param type     配置值类型。
     * @param dictCode 下拉选项对应的字典组编码。
     * @param remarks  面向管理端展示的配置说明。
     */
    void upsertWithDictCode(String key, String value, ConfiguredValueType type, String dictCode, String remarks);

    /**
     * 创建默认系统配置项；配置已存在时不覆盖当前值。
     *
     * @param key     配置键。
     * @param value   仅在配置不存在时使用的初始值。
     * @param type    配置值类型。
     * @param remarks 配置用途说明。
     */
    void ensureExists(String key, String value, ConfiguredValueType type, String remarks);

    /**
     * 查询值。
     *
     * @param key 查询或操作所用的键。
     * @return 可能存在的查询结果。
     */
    Optional<String> findValue(String key);
}
