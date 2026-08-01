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

package com.devops00.spectra.framework.configure.mybatis;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Instant;

/// 元数据填充实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    /// 主键ID
    private static final String ID = "id";
    /// 创建人
    private static final String CREATED_BY = "createdBy";

    /// 创建时间
    private static final String CREATED_AT = "createdAt";

    /// 更新人
    private static final String UPDATED_BY = "updatedBy";

    /// 更新时间
    private static final String UPDATED_AT = "updatedAt";

    /// 统一部门归属字段（存在于带部门隔离的实体中）
    private static final String DEPARTMENT_ID = "departmentId";


    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug(LogPrefix.PERSISTENCE.f("insertFill"));
        if (getFieldValByName(ID, metaObject) == null) {
            setFieldValByName(ID, UuidCreator.getTimeOrderedEpoch(), metaObject);
        }
        if (getFieldValByName(CREATED_BY, metaObject) == null) {
            setFieldValByName(CREATED_BY, SecUtil.getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(CREATED_AT, metaObject) == null) {
            setFieldValByName(CREATED_AT, Instant.now(), metaObject);
        }
        if (getFieldValByName(UPDATED_BY, metaObject) == null) {
            setFieldValByName(UPDATED_BY, SecUtil.getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(UPDATED_AT, metaObject) == null) {
            setFieldValByName(UPDATED_AT, Instant.now(), metaObject);
        }
        // 只要实体声明了 departmentId，就由当前安全用户统一写入，避免客户端伪造归属。
        // 系统任务没有用户上下文时保留显式值，由专用服务/受控执行器负责赋值。
        if (metaObject.hasSetter(DEPARTMENT_ID) && getFieldValByName(DEPARTMENT_ID, metaObject) == null) {
            try {
                var currentUser = SecUtil.getCurrentUser();
                if (currentUser != null && currentUser.getDepartmentId() != null) {
                    setFieldValByName(DEPARTMENT_ID, currentUser.getDepartmentId(), metaObject);
                }
            } catch (IllegalStateException ignored) {
                // Security 策略尚未初始化（例如离线迁移）时不阻断数据库任务。
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug(LogPrefix.PERSISTENCE.f("updateFill"));
        setFieldValByName(UPDATED_BY, SecUtil.getCurrentUserId(), metaObject);
        setFieldValByName(UPDATED_AT, Instant.now(), metaObject);
    }

}
