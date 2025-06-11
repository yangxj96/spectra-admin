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

package com.yangxj96.spectra.core.utils;

import java.util.Collections;
import java.util.Enumeration;

/**
 * 集合工具包扩展
 *
 * @author 杨新杰
 * @since 2025/6/5 10:09
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
