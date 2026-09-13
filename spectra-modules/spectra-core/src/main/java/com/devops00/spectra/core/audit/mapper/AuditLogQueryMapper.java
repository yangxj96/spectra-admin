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

package com.devops00.spectra.core.audit.mapper;

import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryCriteria;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 定义审计日志查询相关的数据库访问操作。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface AuditLogQueryMapper {

    /**
     * 统计符合查询条件的审计日志数量。
     *
     * @param criteria 查询筛选条件。
     * @return 符合条件的数量。
     */
    Long countPage(@Param("criteria") AuditLogQueryCriteria criteria);

    /**
     * 按查询条件分页查询审计日志。
     *
     * @param criteria 查询筛选条件。
     * @param limit    本次查询允许返回的最大记录数。
     * @param offset   查询结果的起始偏移量。
     * @return 符合条件的数据集合。
     */
    List<AuditLogQueryRow> selectPage(@Param("criteria") AuditLogQueryCriteria criteria,
                                      @Param("limit") long limit,
                                      @Param("offset") long offset);

    /**
     * 按查询条件查询审计日志详情。
     *
     * @param criteria   查询筛选条件。
     * @param eventId    审计事件标识。
     * @param occurredAt 审计事件发生时间，用于定位对应的分区记录。
     * @return 查询得到的结果行。
     */
    AuditLogQueryRow selectDetail(@Param("criteria") AuditLogQueryCriteria criteria,
                                  @Param("eventId") UUID eventId,
                                  @Param("occurredAt") Instant occurredAt);

    /**
     * 查询审计日志的导出数据。
     *
     * @param criteria 查询筛选条件。
     * @param limit    本次查询允许返回的最大记录数。
     * @return 符合条件的数据集合。
     */
    List<AuditLogQueryRow> selectExport(@Param("criteria") AuditLogQueryCriteria criteria,
                                        @Param("limit") int limit);
}
