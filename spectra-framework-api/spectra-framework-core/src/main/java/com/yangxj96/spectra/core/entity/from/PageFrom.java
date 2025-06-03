package com.yangxj96.spectra.core.entity.from;

import lombok.*;

/**
 * 分页查询入参
 *
 * @author 杨新杰
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
