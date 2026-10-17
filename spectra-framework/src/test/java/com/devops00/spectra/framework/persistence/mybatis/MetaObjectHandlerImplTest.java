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

package com.devops00.spectra.framework.persistence.mybatis;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MetaObjectHandlerImpl} 使用注入时钟填充审计时间的行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class MetaObjectHandlerImplTest {

    @Test
    void insertFillUsesInjectedClock() {
        Instant fixed = Instant.parse("2020-01-01T00:00:00Z");
        UUID userId = UUID.randomUUID();
        var accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserId()).thenReturn(userId);
        var handler = new MetaObjectHandlerImpl(accessor, Clock.fixed(fixed, ZoneOffset.UTC));
        var entity = new Sample();

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertEquals(fixed, entity.getCreatedAt());
        assertEquals(fixed, entity.getUpdatedAt());
        assertEquals(userId, entity.getCreatedBy());
    }

    @Test
    void updateFillUsesInjectedClock() {
        Instant fixed = Instant.parse("2021-02-03T04:05:06Z");
        var accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserId()).thenReturn(UUID.randomUUID());
        var handler = new MetaObjectHandlerImpl(accessor, Clock.fixed(fixed, ZoneOffset.UTC));
        var entity = new Sample();

        handler.updateFill(SystemMetaObject.forObject(entity));

        assertEquals(fixed, entity.getUpdatedAt());
    }

    /** 可被 MyBatis 元对象读取的最小审计实体。 */
    public static class Sample {
        private UUID id;
        private UUID createdBy;
        private Instant createdAt;
        private UUID updatedBy;
        private Instant updatedAt;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public UUID getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(UUID createdBy) {
            this.createdBy = createdBy;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public UUID getUpdatedBy() {
            return updatedBy;
        }

        public void setUpdatedBy(UUID updatedBy) {
            this.updatedBy = updatedBy;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
