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

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 以持久化实体为边界的通用 Service 契约。
 *
 * <p>业务 Service 继承本接口后获得 MyBatis-Plus 的基础持久化操作，同时保留在业务层声明专用查询和命令的空间。</p>
 *
 * @param <O> 该 Service 管理的持久化实体类型；必须是 {@link BaseEntity} 的子类型
 * @author yangxj96
 * @version 1.0
 * @since 2025-6-14 00:00
 */
public interface BaseService<O extends BaseEntity> extends IService<O> {

}
