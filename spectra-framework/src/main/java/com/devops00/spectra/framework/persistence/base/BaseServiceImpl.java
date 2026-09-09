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

package com.devops00.spectra.framework.persistence.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用持久化 Service 的默认实现。
 *
 * <p>通过 MyBatis-Plus Mapper 委托基础 CRUD；业务实现类只需指定 Mapper 和实体类型并补充领域操作。</p>
 *
 * @param <M> 该 Service 使用的 MyBatis-Plus Mapper 类型
 * @param <O> 该 Service 管理的持久化实体类型；必须是 {@link BaseEntity} 的子类型
 * @author yangxj96
 * @version 1.0
 * @since 2025-6-14 00:00
 */
@Slf4j
public class BaseServiceImpl<M extends BaseMapper<O>, O extends BaseEntity> extends ServiceImpl<M, O> implements BaseService<O> {

}
