package com.yangxj96.spectra.kernel.entity.from;

import com.yangxj96.spectra.core.base.Verify;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 菜单保存接口
 *
 * @author 杨新杰
 * @since 2025/6/4 10:12
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuSaveFrom {

    /**
     * 数据id
     */
    @NotNull(message = "ID不能为空", groups = Verify.Update.class)
    @Null(message = "新增时不能有ID存在", groups = Verify.Insert.class)
    private Long id;

    /**
     * 父级ID
     */
    private Long pid;

    /**
     * 图标
     */
    @NotNull(message = "图标不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private String icon;

    /**
     * 名称
     */
    @NotBlank(message = "菜单名称不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private String name;

    /**
     * 请求路径
     */
    @NotBlank(message = "请求路径不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
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
    @NotNull(message = "排序不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private Integer sort;
}
