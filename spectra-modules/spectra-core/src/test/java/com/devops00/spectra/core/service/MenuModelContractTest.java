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

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.javabean.vo.MenuVO;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 菜单模型收缩契约测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/30
 */
class MenuModelContractTest {

    private static final Set<String> LEGACY_FIELDS = Set.of("path", "component", "layout", "hide", "metadata");

    @Test
    void menuModelsShouldNotExposeLegacyRouteFields() {
        for (Class<?> type : Set.of(Menu.class, MenuSaveFrom.class, MenuVO.class, MenuTreeVO.class)) {
            var fieldNames = Arrays.stream(type.getDeclaredFields()).map(field -> field.getName()).toList();
            assertTrue(fieldNames.stream().noneMatch(LEGACY_FIELDS::contains), type.getSimpleName() + " 仍包含旧菜单字段");
        }
    }

    @Test
    void menuShouldNotEnableAutoResultMapAfterMetadataRemoval() {
        var tableName = Menu.class.getAnnotation(TableName.class);
        assertFalse(tableName.autoResultMap());
    }

    @Test
    void menuMapperShouldNotReferenceLegacyColumns() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream("mapper/system/MenuMapper.xml")) {
            assertTrue(input != null, "未找到 MenuMapper.xml");
            var xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String field : LEGACY_FIELDS) {
                assertFalse(xml.matches("(?s).*\\b" + field + "\\b.*"), "MenuMapper.xml 仍引用旧字段 " + field);
            }
        }
    }
}
