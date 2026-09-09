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
import com.devops00.spectra.common.foundation.collection.CollUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * REST 分页查询的通用入参。
 *
 * <p>调用方提供页码、每页数量和可选排序字段；{@link #toPage()} 将这些值转换为 MyBatis-Plus 查询使用的分页对象，
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
    private List<OrderItem> orders;

    /**
     * 转换为 MyBatis-Plus 查询使用的分页参数。
     *
     * @param <T> 查询结果中的记录类型
     * @return 使用当前页码、每页数量和排序条件创建的分页对象；即使未提供排序条件也不会返回 null
     */
    public <T> Page<T> toPage() {
        var page = new Page<T>(this.pageNum, this.pageSize);
        if (CollUtils.isNotEmpty(this.orders)) {
            page.setOrders(this.orders);
        }
        return page;
    }
}
