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

package com.devops00.spectra.framework.persistence.pagination;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * REST 分页查询的通用入参。
 *
 * <p>调用方提供页码、每页数量和可选排序字段；{@link #toPage(Map)} 将这些值转换为 MyBatis-Plus 查询使用的分页对象，
 * 不承载具体业务筛选条件。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/3 00:00
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageFrom {

    private static final Pattern SQL_COLUMN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)?");

    private static final String INVALID_SORT = "不支持的分页排序字段";

    /**
     * 每页最多返回的记录数；未传入时使用 15。
     */
    private Long pageSize = 15L;

    /**
     * 要查询的页码，从 1 开始；未传入时查询第 1 页。
     */
    private Long pageNum = 1L;

    /**
     * 前端请求的排序字段；为 null 或空列表时不额外设置排序条件。
     */
    private List<PageOrderFrom> orders;

    /**
     * 转换为 MyBatis-Plus 查询使用的分页参数。
     *
     * @param allowedSortFields 当前用例允许的公开字段到可信 SQL 列名的映射；连接查询在值中声明表别名
     * @param <T>               查询结果中的记录类型
     * @return 使用当前页码、每页数量和排序条件创建的分页对象；即使未提供排序条件也不会返回 null
     */
    public <T> Page<T> toPage(Map<String, String> allowedSortFields) {
        if (allowedSortFields == null) {
            throw new IllegalStateException("分页排序字段映射未配置");
        }
        var page = new Page<T>(this.pageNum, this.pageSize);
        if (this.orders == null || this.orders.isEmpty()) {
            return page;
        }
        var resolved = new ArrayList<OrderItem>();
        var seen = new HashSet<String>();
        for (var order : this.orders) {
            String column = resolveColumn(order, allowedSortFields);
            if (!seen.add(column)) {
                throw new IllegalArgumentException(INVALID_SORT);
            }
            resolved.add(order.isAsc() ? OrderItem.asc(column) : OrderItem.desc(column));
        }
        page.setOrders(resolved);
        return page;
    }

    /** 固定排序的用例必须明确拒绝客户端排序，不能静默忽略请求。 */
    public void requireUnsorted() {
        if (this.orders != null && !this.orders.isEmpty()) {
            throw new IllegalArgumentException(INVALID_SORT);
        }
    }

    /** 请求仅用于查找字段映射，进入 SQL 的列名完全由服务端提供。 */
    private static String resolveColumn(PageOrderFrom order, Map<String, String> allowedSortFields) {
        if (order == null || order.getColumn() == null || order.getColumn().isBlank()) {
            throw new IllegalArgumentException(INVALID_SORT);
        }
        String column = allowedSortFields.get(order.getColumn());
        if (column == null) {
            throw new IllegalArgumentException(INVALID_SORT);
        }
        if (!SQL_COLUMN.matcher(column).matches()) {
            throw new IllegalStateException("分页排序字段映射无效");
        }
        return column;
    }
}
