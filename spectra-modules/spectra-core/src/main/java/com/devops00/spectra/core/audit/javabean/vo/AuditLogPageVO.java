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

package com.devops00.spectra.core.audit.javabean.vo;

import java.util.List;

/**
 * 封装审计日志分页相关的响应数据。
 *
 * @param records 当前页包含的记录集合
 * @param total   符合查询条件的记录总数
 * @param current 当前页码
 * @param size    当前页的记录数量
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record AuditLogPageVO(List<AuditLogVO> records, long total, long current, long size) {

    public AuditLogPageVO {
        records = records == null ? List.of() : List.copyOf(records);
    }
}
