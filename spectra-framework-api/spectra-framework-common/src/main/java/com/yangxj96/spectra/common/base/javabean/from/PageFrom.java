package com.yangxj96.spectra.common.base.javabean.from;

import lombok.*;

/**
 * 分页查询入参
 *
 * @author Jack Young
 * @since 2025/6/3 23:21
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

}
