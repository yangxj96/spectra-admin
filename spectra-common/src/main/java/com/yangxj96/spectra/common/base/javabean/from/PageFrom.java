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

package com.yangxj96.spectra.common.base.javabean.from;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 分页查询入参
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/3
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageFrom {

    /**
     * 页码
     */
    private Long pageSize;

    /**
     * 每页数量
     */
    private Long pageNum;

    /**
     * 排序字段,前端
     */
    private List<OrderItem> orders;

    public <T> Page<T> toPage() {
        Page<T> page = new Page<>(this.pageNum, this.pageSize);
        if (CollectionUtils.isNotEmpty(orders)) {
            page.setOrders(this.orders);
        }
        return page;
    }
}
