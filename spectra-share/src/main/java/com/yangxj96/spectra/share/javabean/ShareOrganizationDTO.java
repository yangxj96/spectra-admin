package com.yangxj96.spectra.share.javabean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组织机构共享用的DTO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareOrganizationDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 上级ID
     */
    private Long pid;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 组织机构类型
     */
    private Short type;

    /**
     * 组织机构所在地址
     */
    private String address;

    /**
     * 负责人ID
     */
    private Long managerId;

    /**
     * 备注
     */
    private String remark;

}
