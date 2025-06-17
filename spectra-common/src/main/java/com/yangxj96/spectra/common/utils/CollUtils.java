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

package com.yangxj96.spectra.common.utils;

import java.util.Collections;
import java.util.Enumeration;

/**
 * 集合工具包扩展
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public final class CollUtils {

    private CollUtils() {
    }

    /**
     * 判断迭代器中是否包含目标
     *
     * @param enumeration 迭代器
     * @param target      目标
     * @param <T>         泛型
     * @return 是否包含
     */
    public static <T> boolean contains(Enumeration<T> enumeration, T target) {
        if (enumeration == null) {
            return false;
        }
        return Collections.list(enumeration)
                .stream()
                .anyMatch(item -> (target == null && item == null) || (target != null && target.equals(item)));
    }

}
