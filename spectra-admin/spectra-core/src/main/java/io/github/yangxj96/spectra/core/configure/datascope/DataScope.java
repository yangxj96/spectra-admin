package io.github.yangxj96.spectra.core.configure.datascope;

import java.lang.annotation.*;

/**
 * 数据范围注解
 * <pre>
 *     <ul>
 *         <li>角色之间的数据范围是并集</li>
 *         <li>用户直授与角色数据范围是并集</li>
 *         <li>除非 ALL，否则所有条件 OR 连接</li>
 *     </ul>
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 组织字段名（表别名.字段）
     * 例：org_id / dept_id / o.org_id
     */
    String orgField() default "ORGANIZATION_ID";

    /**
     * 创建人字段（SELF 使用）
     */
    String userField() default "CREATED_BY";

    /**
     * 是否启用（方便调试 / 特殊场景关闭）
     */
    boolean enabled() default true;
}
