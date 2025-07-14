package com.yangxj96.spectra.service.core.javabean.from;

import com.yangxj96.spectra.common.base.Verify;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组织机构入参
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationFrom {

    @NotNull(message = "ID不能为空", groups = Verify.Update.class)
    @Null(message = "新增时不能有ID存在", groups = Verify.Insert.class)
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
