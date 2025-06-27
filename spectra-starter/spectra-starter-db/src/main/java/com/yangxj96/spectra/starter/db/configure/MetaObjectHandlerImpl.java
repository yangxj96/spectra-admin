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

package com.yangxj96.spectra.starter.db.configure;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.yangxj96.spectra.common.constant.Common;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 元数据填充实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    /**
     * 创建人
     */
    private static final String CREATED_BY = "createdBy";

    /**
     * 创建时间
     */
    private static final String CREATED_AT = "createdAt";

    /**
     * 更新人
     */
    private static final String UPDATED_BY = "updatedBy";

    /**
     * 更新时间
     */
    private static final String UPDATED_AT = "updatedAt";


    @Override
    public void insertFill(MetaObject metaObject) {
        log.atDebug().log("[MyBatisPlus] 新增填充字段");
        if (getFieldValByName(CREATED_BY, metaObject) == null) {
            setFieldValByName(CREATED_BY, getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(CREATED_AT, metaObject) == null) {
            setFieldValByName(CREATED_AT, LocalDateTime.now(), metaObject);
        }
        if (getFieldValByName(UPDATED_BY, metaObject) == null) {
            setFieldValByName(UPDATED_BY, getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(UPDATED_AT, metaObject) == null) {
            setFieldValByName(UPDATED_AT, LocalDateTime.now(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.atDebug().log("[MyBatisPlus] 修改填充字段");
        setFieldValByName(UPDATED_BY, getCurrentUserId(), metaObject);
        setFieldValByName(UPDATED_AT, LocalDateTime.now(), metaObject);
    }

    /**
     * 获取当前用户ID,如果获取失败,则返回ID为0
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.atError().log("获取ID出错,默认ID为{}", Common.PID);
            return Common.PID;
        }
    }
}
