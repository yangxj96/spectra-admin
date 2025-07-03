/*
 *  Copyright 2025 yangxj96.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.yangxj96.spectra.service.core.javabean.from;

import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.enums.CommonState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 字典类型入参
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictGroupFrom {

    /**
     * 主键ID
     */
    @NotNull(message = "ID不能为空", groups = Verify.Update.class)
    @Null(message = "新增时不能有ID存在", groups = Verify.Insert.class)
    private Long id;

    /**
     * 父级ID
     */
    private Long pid;

    /**
     * 字典名称
     */
    @NotNull(message = "字典类型名称不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String name;

    /**
     * 字典编码
     */
    @NotNull(message = "字典类型名称不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String code;

    /**
     * 字典状态
     */
    @NotNull(message = "字典类型名称不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private CommonState state;

    /**
     * 备注
     */
    private String remark;

}
