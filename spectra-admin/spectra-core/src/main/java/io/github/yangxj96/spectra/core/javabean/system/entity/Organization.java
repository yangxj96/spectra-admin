/*
 *  Copyright 2018-2025 yangxj96
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
 */

package io.github.yangxj96.spectra.core.javabean.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织机构
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-15
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "SYS_ORGANIZATION")
public class Organization extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 上级ID
     */
    @TableField(value = "PID")
    private Long pid;

    /**
     * 名称
     */
    @TableField(value = "NAME")
    private String name;

    /**
     * 编码
     * <p>插入时候生成,后续不参与更新等操作</p>
     */
    @TableField(value = "CODE", insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NEVER)
    private String code;

    /**
     * 组织机构类型
     */
    @TableField(value = "TYPE")
    private Short type;

    /**
     * 构建路径
     * <p>格式:比如总部/二级/三级/部门</p>
     */
    @TableField(value = "PATH")
    private String path;

    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;
}

