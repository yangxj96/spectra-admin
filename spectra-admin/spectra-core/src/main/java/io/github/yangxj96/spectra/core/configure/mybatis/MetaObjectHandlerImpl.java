/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.configure.mybatis;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.yangxj96.spectra.core.template.SecurityTemplate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Instant;

/**
 * 元数据填充实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    private static final String PREFIX = "[MetaObjectHandler]:";

    @Resource
    private SecurityTemplate securityTemplate;

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
        log.debug("{}insertFill", PREFIX);
        if (getFieldValByName(CREATED_BY, metaObject) == null) {
            setFieldValByName(CREATED_BY, securityTemplate.getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(CREATED_AT, metaObject) == null) {
            setFieldValByName(CREATED_AT, Instant.now(), metaObject);
        }
        if (getFieldValByName(UPDATED_BY, metaObject) == null) {
            setFieldValByName(UPDATED_BY, securityTemplate.getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(UPDATED_AT, metaObject) == null) {
            setFieldValByName(UPDATED_AT, Instant.now(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("{}updateFill", PREFIX);
        setFieldValByName(UPDATED_BY, securityTemplate.getCurrentUserId(), metaObject);
        setFieldValByName(UPDATED_AT, Instant.now(), metaObject);
    }

}
