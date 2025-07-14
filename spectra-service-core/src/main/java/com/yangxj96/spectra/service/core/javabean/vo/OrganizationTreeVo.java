package com.yangxj96.spectra.service.core.javabean.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yangxj96.spectra.common.base.javabean.vo.Tree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 组织机构树形
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTreeVo implements Tree<OrganizationTreeVo>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 上级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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

    /**
     * 下级菜单
     */
    private List<OrganizationTreeVo> children;
}
