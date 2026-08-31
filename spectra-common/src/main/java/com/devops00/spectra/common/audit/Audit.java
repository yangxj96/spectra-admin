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

package com.devops00.spectra.common.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统一审计注解。
 *
 * <p>新代码优先使用该注解；复杂业务动作直接调用 {@link AuditService}。事件类型应保持稳定，
 * 为空时由技术入口根据声明方法生成。{@code value} 只用于描述或原因表达式，不应放入密码、Token
 * 或其他敏感凭据。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /**
     * 操作说明或原因；支持只读 SpEL。
     */
    String value() default "";

    /**
     * 稳定的审计事件类型；为空时由切面按方法生成。
     */
    String eventType() default "";

    /**
     * 审计可靠性分类。
     */
    AuditCategory category() default AuditCategory.OPERATION;
}
