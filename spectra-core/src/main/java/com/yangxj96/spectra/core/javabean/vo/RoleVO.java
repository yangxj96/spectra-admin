package com.yangxj96.spectra.core.javabean.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yangxj96.spectra.common.enums.PowerScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色响应VO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 名称
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 状态
     */
    @TableField(value = "\"state\"")
    private Boolean state;

    /**
     * 范围
     */
    @TableField(value = "\"scope\"")
    private PowerScope scope;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

}
