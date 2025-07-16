package com.yangxj96.spectra.share.javabean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 共享权限信息
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareAuthority {

    private Long id;

    /**
     * 父级ID,用于构建树形结构
     */
    private Long pid;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

}
