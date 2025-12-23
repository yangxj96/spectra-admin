package io.github.yangxj96.spectra.core.javabean.user.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.core.configure.datascope.DataScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 用户数据范围
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/23 11:24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "SYS_USER_DATA_SCOPE")
public class UserDataScope extends BaseEntity implements Serializable {

    /**
     * 用户ID
     */
    @TableField(value = "USER_ID")
    private Long userId;

    /**
     * 数据范围类型
     */
    @TableField(value = "SCOPE_TYPE")
    private DataScopeType scopeType;

}
