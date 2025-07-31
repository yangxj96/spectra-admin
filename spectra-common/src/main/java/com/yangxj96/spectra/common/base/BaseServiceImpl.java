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

package com.yangxj96.spectra.common.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * RESTFul 接口公用service层
 *
 * @param <M> 子类对应的mapper
 * @param <O> 子类对应的实体
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
public class BaseServiceImpl<M extends BaseMapper<O>, O extends BaseEntity>
        extends ServiceImpl<M, O> implements BaseService<O> {

    @Override
    public LambdaQueryWrapper<O> getWrapper() {
        var wrapper = new QueryWrapper<O>();
        // 检查实体类 O 是否实现了 ScopeEntity 接口
        if (ScopeEntity.class.isAssignableFrom(getEntityClass())) {
            // O 确实实现了 ScopeEntity
            // 在这里添加你的数据权限逻辑
            log.atDebug().log("实体 " + getEntityClass().getSimpleName() + " 实现了 ScopeEntity，应用数据权限过滤");
            // 示例：假设你要添加 organizationId 等于当前用户的条件
            // Long currentOrgId = getCurrentUserOrganizationId(); // 你需要实现这个方法
            // if (currentOrgId != null) {
            //     wrapper.eq("organization_id", currentOrgId); // 注意：这里使用字段名，不是属性名
            //     // 或者使用 LambdaQueryWrapper 的方式 (需要 O 是 ScopeEntity 的子类型，但编译器不知道)
            //     // 由于类型擦除，直接使用 wrapper.eq(O::getOrganizationId, ...) 在这里编译器会报错，因为 O 不一定是 ScopeEntity
            //     // 所以通常使用字符串方式，或者进行类型转换（不推荐，因为 O 是泛型)
            // }
            // 更安全的方式是使用字符串，或者在具体子类中覆盖 getWrapper 方法。

            // **重要**：由于 O 是泛型，编译器只知道它 extends BaseEntity，不知道它有 getOrganizationId。
            // 所以你不能直接写 wrapper.eq(O::getOrganizationId, ...).
            // 解决方案：
            //  1. 使用字符串：wrapper.eq("organization_id", currentOrgId);
            //  2. 在具体 Service 实现类中覆盖 getWrapper()，在那里你可以安全地使用 Lambda 表达式。
            //  3. 使用反射调用 getOrganizationId (性能开销大，不推荐)。

        } else {
            log.atDebug().log("实体 " + getEntityClass().getSimpleName() + " 未实现 ScopeEntity，不应用数据权限过滤");
        }
        return wrapper.lambda();
    }

}
