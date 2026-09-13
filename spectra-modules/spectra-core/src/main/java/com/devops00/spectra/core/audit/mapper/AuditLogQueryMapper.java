/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.audit.mapper;

import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryCriteria;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** XML-backed reads from the partitioned audit event table. */
@Mapper
public interface AuditLogQueryMapper {

    Long countPage(@Param("criteria") AuditLogQueryCriteria criteria);

    List<AuditLogQueryRow> selectPage(@Param("criteria") AuditLogQueryCriteria criteria,
                                      @Param("limit") long limit,
                                      @Param("offset") long offset);

    AuditLogQueryRow selectDetail(@Param("criteria") AuditLogQueryCriteria criteria,
                                  @Param("eventId") UUID eventId,
                                  @Param("occurredAt") Instant occurredAt);

    List<AuditLogQueryRow> selectExport(@Param("criteria") AuditLogQueryCriteria criteria,
                                        @Param("limit") int limit);
}
