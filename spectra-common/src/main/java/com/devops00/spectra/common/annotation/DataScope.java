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

package com.devops00.spectra.common.annotation;

import java.lang.annotation.*;

/**
 * 数据范围注解 — 声明实体如何参与数据权限过滤
 * <p>
 * 标注在 Entity 类上，DataScopeInnerInterceptor 根据此注解自动生成 WHERE 条件。
 *
 * <h3>结构维度</h3> 通过 {@link #column()} 指定归属字段（默认 department_id）。 查询时自动加上
 * {@code WHERE column IN (scopeDepts)} 或 {@code WHERE column = userDeptId}。
 *
 * <h3>关系维度</h3> 通过 {@link #relations()} 声明多对多关联表。 查询时自动加上
 * {@code OR id IN (SELECT ... FROM relationTable WHERE user_id = ?)}。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * SELECT/list/detail 使用的 Permission。为空时资源不会进入 SQL 隔离注册表。
     */
    String readPermission() default "";

    /**
     * UPDATE/DELETE/BATCH 使用的 Permission。写操作不得隐式借用 read Permission。
     */
    String writePermission() default "";

    /**
     * EXPORT 使用的 Permission。导出必须显式登记，不能复用普通查询权限。
     */
    String exportPermission() default "";

    /**
     * 结构维度过滤字段名，默认 department_id
     */
    String column() default "department_id";

    /**
     * SELF 范围使用的归属字段，默认使用审计字段 created_by。
     * <p>
     * 结构字段与本人字段不是同一个概念：例如会议按 department_id 隔离， 但本人范围应按
     * created_by；关联明细则可以显式指定 user_id。
     */
    String ownerColumn() default "created_by";

    /**
     * 关系维度——本实体涉及的关联表
     */
    Relation[] relations() default {};

    @interface Relation {
        /**
         * 关联表所在 schema。为空时使用当前连接的 search_path。
         */
        String schema() default "";

        /**
         * 关联表名
         */
        String table();

        /**
         * 关联表中指向本实体的外键字段
         */
        String joinColumn();

        /**
         * 关联表中的用户标识字段
         */
        String userColumn() default "user_id";

        /**
         * 主表用于 IN 匹配的列名（默认 id，MeetingRecord 等关联需用 meeting_id）
         */
        String mainColumn() default "id";

        /**
         * 关联表的部门归属列；用于没有自身 department_id 的明细资源。
         */
        String departmentColumn() default "";
    }
}
