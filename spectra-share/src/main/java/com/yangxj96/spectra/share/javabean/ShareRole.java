package com.yangxj96.spectra.share.javabean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 共享角色信息
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareRole {

    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 范围
     */
    private Short scope;

    /**
     * 备注
     */
    private String remark;
}
