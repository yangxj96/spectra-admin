package io.github.yangxj96.spectra.core.user.javabean.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityVO {

    /**
     * 数据id.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 父级ID,用于构建树形结构
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
