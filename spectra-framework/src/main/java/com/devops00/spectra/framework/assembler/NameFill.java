/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.framework.assembler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 字段名称填充注解
///
/// 用于 VO（View Object）层，在仅持有 ID 字段的情况下，
/// 通过指定的 {@link NameLookup} 实现类，
/// 在响应阶段自动填充对应的「名称 / 展示值」字段。
///
/// 设计目标：
/// * 避免在实体或 VO 中引入多余的冗余字段
/// * 避免在 SQL 层进行强耦合 JOIN
/// * 将「ID → Name」的装配逻辑统一收敛到 Assembler 层
///
/// 使用约束：
/// * 只能用于 VO 层字段
/// * 仅用于<strong>展示型字段</strong>，不得参与业务判断
/// * lookup 必须是 {@link NameLookup} 的具体实现类（而非 Service 接口）
///
/// 示例：
/// <pre>
/// {@code
/// @NameFill(
///     lookup = DeptNameLookup.class,
///     sourceField = "deptId"
/// )
/// private String deptName;
/// }
/// </pre>
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/2 16:26
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameFill {

    /// 指定用于执行「ID → Name」映射的 Lookup 实现类
    ///
    /// 必须是 {@link NameLookup} 的具体实现类，
    /// 而不是 Service 接口或抽象类，
    /// 以确保语义清晰、职责单一。
    Class<? extends NameLookup<?>> lookup();

    /// VO 中用于取值的 ID 字段名
    ///
    /// 该字段必须存在于当前 VO 类中，
    /// 且其类型需与 lookup.idType() 返回的类型一致。
    String sourceField();

}
