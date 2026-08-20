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

package com.devops00.spectra.framework;

import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.framework.configure.mybatis.MetaObjectHandlerImpl;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MyBatis-Plus 全局新增字段填充测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
class MetaObjectHandlerImplTest {

    @Test
    void insertFillGeneratesUuidV7ForEmptyBaseEntityId() {
        var entity = new BaseEntity();
        var handler = new MetaObjectHandlerImpl(emptySecurityContext());

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertNotNull(entity.getId());
        assertEquals(7, entity.getId().version());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void insertFillDoesNotReplaceAnExistingId() {
        UUID existingId = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae3");
        var entity = new BaseEntity();
        entity.setId(existingId);
        var handler = new MetaObjectHandlerImpl(emptySecurityContext());

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertEquals(existingId, entity.getId());
    }

    private static SecurityContextAccessor emptySecurityContext() {
        return new SecurityContextAccessor() {
            @Override
            public com.devops00.spectra.security.base.javabean.entity.SecurityUser currentUser() {
                return null;
            }

            @Override
            public UUID currentUserId() {
                return null;
            }

            @Override
            public String currentToken() {
                return null;
            }

            @Override
            public String currentUserZoneId() {
                return "";
            }

            @Override
            public String currentUsername() {
                return "";
            }
        };
    }
}
