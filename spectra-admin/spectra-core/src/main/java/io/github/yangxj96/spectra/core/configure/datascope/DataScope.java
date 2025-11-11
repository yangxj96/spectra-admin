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

package io.github.yangxj96.spectra.core.configure.datascope;

import java.lang.annotation.*;

/**
 * 数据范围注解
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * 是否进行过滤
     * <p>默认需要过滤,预留这个接口主要是为了临时方法不过滤使用</p>
     *
     * @return 是否进行过滤
     */
    boolean filter() default true;
}
