package io.github.yangxj96.spectra.core.system.javabean.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    /**
     * 数据id.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 父级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 图标
     */
    private String icon;

    /**
     * 名称
     */
    private String name;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 组件路径,为空则使用布局组件
     */
    private String component;

    /**
     * 布局
     */
    private String layout;

    /**
     * 排序
     */
    private Integer sort;

}
