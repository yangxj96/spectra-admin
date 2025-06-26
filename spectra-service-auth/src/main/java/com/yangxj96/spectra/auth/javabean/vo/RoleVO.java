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

package com.yangxj96.spectra.auth.javabean.vo;

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
