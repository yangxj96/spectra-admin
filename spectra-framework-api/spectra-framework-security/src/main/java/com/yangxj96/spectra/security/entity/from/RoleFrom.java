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

package com.yangxj96.spectra.security.entity.from;

import com.yangxj96.spectra.core.base.Verify;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 角色操作入参
 *
 * @author 杨新杰
 * @since 2025/6/9 23:47
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleFrom {

    /**
     * 角色ID
     */
    @Null(message = "新增不能指定角色ID", groups = Verify.Insert.class)
    @NotNull(message = "角色ID不能为空", groups = Verify.Update.class)
    private Long id;

    /**
     * 角色名称
     */
    @NotEmpty(message = "用户名不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 范围
     */
    @Min(value = 0, message = "范围值不正确", groups = {Verify.Insert.class, Verify.Update.class})
    @Max(value = 2, message = "范围值不正确", groups = {Verify.Insert.class, Verify.Update.class})
    private Short scope;

    /**
     * 备注
     */
    private String remark;

}
