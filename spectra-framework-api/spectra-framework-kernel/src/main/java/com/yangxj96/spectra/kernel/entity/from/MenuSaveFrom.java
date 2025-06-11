/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
