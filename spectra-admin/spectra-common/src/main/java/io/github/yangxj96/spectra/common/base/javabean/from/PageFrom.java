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

package io.github.yangxj96.spectra.common.base.javabean.from;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import lombok.*;

import java.util.List;

/// 分页查询入参
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/3
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageFrom {

    /// 页码
    @Builder.Default
    private Long pageSize = 10L;

    /// 每页数量
    @Builder.Default
    private Long pageNum = 1L;

    /// 排序字段,前端传递的
    private List<OrderItem> orders;

    /// 转换成mybatis plus分页查询用的分页参数
    ///
    /// @param <T> 具体类型
    /// @return 分页参数对象
    public <T> Page<T> toPage() {
        var page = new Page<T>(this.pageNum, this.pageSize);
        if (CollUtils.isNotEmpty(this.orders)) {
            page.setOrders(this.orders);
        }
        return page;
    }
}
