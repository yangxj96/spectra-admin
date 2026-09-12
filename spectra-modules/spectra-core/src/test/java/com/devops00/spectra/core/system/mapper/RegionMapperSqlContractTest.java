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

package com.devops00.spectra.core.system.mapper;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/** 行政区域递归路径查询 SQL 契约测试。 */
class RegionMapperSqlContractTest {

    @Test
    void usesRecursivePathQueryWithDepthLimitAndCycleGuard() throws IOException {
        String mapper = readMapper();

        assertThat(mapper).contains("<select id=\"selectPath\"")
                .contains("WITH RECURSIVE region_path AS")
                .contains("ARRAY[id] AS visited")
                .contains("path_node.visited || ARRAY[parent.id] AS visited")
                .contains("path_node.depth &lt;= #{maxDepth}")
                .contains("NOT (parent.id = ANY (path_node.visited))")
                .contains("ORDER BY depth ASC")
                .contains("deleted IS NULL");
    }

    @Test
    void recursivePathQueryCanBeParsedByMybatisPermissionParser() throws IOException {
        Matcher select = Pattern.compile("(?s)<select id=\"selectPath\"[^>]*>(.*?)</select>")
                .matcher(readMapper());
        assertThat(select.find()).isTrue();

        String sql = select.group(1)
                .replaceAll("(?s)<!--.*?-->", " ")
                .replaceAll("<[^>]+>", " ")
                .replace("&lt;", "<")
                .replace("#{id}", "?")
                .replace("#{maxDepth}", "?")
                .replaceAll("\\s+", " ")
                .trim();

        assertThatNoException().isThrownBy(() -> CCJSqlParserUtil.parse(sql));
    }

    private static String readMapper() throws IOException {
        try (var resource = RegionMapperSqlContractTest.class.getClassLoader()
                .getResourceAsStream("mapper/system/RegionMapper.xml")) {
            if (resource == null) {
                throw new IOException("找不到行政区域 Mapper XML");
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
