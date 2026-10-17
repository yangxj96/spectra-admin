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

package com.devops00.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证可清空字段显式使用 ALWAYS 更新策略，避免 null 被默认策略忽略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/17
 */
class NullableFieldUpdateStrategyTest {

    @Test
    void menuNullableFieldsAlwaysUpdate() throws Exception {
        assertAlways(Menu.class, "pid");
        assertAlways(Menu.class, "routeName");
    }

    @Test
    void regionNullableTextFieldsAlwaysUpdate() throws Exception {
        assertAlways(Region.class, "fullName");
        assertAlways(Region.class, "shortName");
        assertAlways(Region.class, "code");
        assertAlways(Region.class, "path");
        assertAlways(Region.class, "pid");
    }

    @Test
    void configuredRemarksAlwaysUpdate() throws Exception {
        assertAlways(Configured.class, "remarks");
    }

    private static void assertAlways(Class<?> type, String field) throws Exception {
        assertEquals(FieldStrategy.ALWAYS,
                type.getDeclaredField(field).getAnnotation(TableField.class).updateStrategy(),
                type.getSimpleName() + "." + field);
    }
}
