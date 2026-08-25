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

package com.devops00.spectra.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.constant.RegionLevel;
import com.devops00.spectra.core.system.javabean.entity.Region;
import com.devops00.spectra.core.system.service.RegionService;
import com.devops00.test.spectra.RegionImportTestApplication;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 手工行政区划数据导入夹具。
 *
 * <p>只有显式设置 {@code SPECTRA_REGION_IMPORT=true} 时才启用，避免普通测试或应用构建修改数据库。
 * 导入按层级顺序执行，JSON 逐条解析，单批写入后立即释放对象，避免将 62 万条村级数据一次性加载到内存。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/1/30 14:01
 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_REGION_IMPORT", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = RegionImportTestApplication.class)
public class RegionServiceTest {

    private static final int BATCH_SIZE = 500;

    private static final int PROVINCE_COUNT = 31;

    private static final int CITY_COUNT = 342;

    private static final int AREA_COUNT = 2_978;

    private static final int STREET_COUNT = 41_352;

    private static final int VILLAGE_COUNT = 620_573;

    private static final UUID SEED_ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final Instant SEED_TIMESTAMP = Instant.parse("1996-10-14T16:00:00Z");

    @Resource
    private RegionService regionService;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void importProvinces() throws IOException {
        if (regionService.count() != 0) {
            throw new IllegalStateException("sys_region 非空，已拒绝重复导入；请确认目标数据库后再处理已有数据");
        }
        importRegions("regions/provinces.json", RegionLevel.PROVINCES, null, null, PROVINCE_COUNT);
    }

    @Test
    @Order(2)
    void importCities() throws IOException {
        importRegions("regions/cities.json", RegionLevel.CITIES, RegionLevel.PROVINCES, "provinceCode", CITY_COUNT);
    }

    @Test
    @Order(3)
    void importAreas() throws IOException {
        importRegions("regions/areas.json", RegionLevel.AREAS, RegionLevel.CITIES, "cityCode", AREA_COUNT);
    }

    @Test
    @Order(4)
    void importStreets() throws IOException {
        importRegions("regions/streets.json", RegionLevel.STREETS, RegionLevel.AREAS, "areaCode", STREET_COUNT);
    }

    @Test
    @Order(5)
    void importVillages() throws IOException {
        importRegions("regions/villages.json", RegionLevel.VILLAGES, RegionLevel.STREETS, "streetCode", VILLAGE_COUNT);
    }

    /**
     * 读取并导入一个层级的区域数据。
     *
     * @param resourceName    资源文件名
     * @param level           当前层级
     * @param parentLevel     上级层级；省级为 {@code null}
     * @param parentCodeField JSON 中的上级编码字段；省级为 {@code null}
     * @param expectedCount   当前资源的预期条数
     * @throws IOException 资源读取失败
     */
    private void importRegions(
                               String resourceName,
                               RegionLevel level,
                               RegionLevel parentLevel,
                               String parentCodeField,
                               int expectedCount)
            throws IOException {
        int sourceCount = countRecords(resourceName);
        if (sourceCount != expectedCount) {
            throw new IllegalStateException(
                    "区域资源 " + resourceName + " 条数不符合预期：expected=" + expectedCount + ", actual=" + sourceCount);
        }

        Map<String, Region> parentMap = loadParentMap(parentLevel);
        List<Region> batch = new ArrayList<>(BATCH_SIZE);
        int importedCount = 0;

        try (var inputStream = resource(resourceName).getInputStream();
                JsonParser parser = objectMapper.createParser(inputStream)) {
            requireArrayStart(parser, resourceName);
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.currentToken() != JsonToken.START_OBJECT) {
                    throw invalidResource(resourceName, importedCount + 1, "数组元素必须是对象");
                }
                JsonNode node = parser.readValueAsTree();
                Region region = toRegion(node, resourceName, importedCount + 1, level, parentMap, parentCodeField);
                batch.add(region);
                importedCount++;

                if (batch.size() == BATCH_SIZE) {
                    saveBatch(batch, resourceName, importedCount - batch.size() + 1, importedCount);
                }
            }
            requireEndOfDocument(parser, resourceName);
        }

        saveBatch(batch, resourceName, importedCount - batch.size() + 1, importedCount);
        if (importedCount != expectedCount) {
            throw new IllegalStateException(
                    "区域资源 " + resourceName + " 导入条数不符合预期：expected=" + expectedCount + ", actual=" + importedCount);
        }

        long persistedCount = regionService.lambdaQuery().eq(Region::getLevel, level).count();
        if (persistedCount != importedCount) {
            throw new IllegalStateException(
                    "区域资源 " + resourceName + " 写入后数量不一致：expected=" + importedCount + ", actual=" + persistedCount);
        }
    }

    /**
     * 先以流式方式统计资源条数，确保数据包版本变化时不会先写入部分数据。
     *
     * @param resourceName 资源文件名
     * @return JSON 数组元素数量
     * @throws IOException 资源读取失败
     */
    private int countRecords(String resourceName) throws IOException {
        try (var inputStream = resource(resourceName).getInputStream();
                JsonParser parser = objectMapper.createParser(inputStream)) {
            requireArrayStart(parser, resourceName);
            int count = 0;
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.currentToken() != JsonToken.START_OBJECT) {
                    throw invalidResource(resourceName, count + 1, "数组元素必须是对象");
                }
                parser.readValueAsTree();
                count++;
            }
            requireEndOfDocument(parser, resourceName);
            return count;
        }
    }

    private Map<String, Region> loadParentMap(RegionLevel parentLevel) {
        if (parentLevel == null) {
            return Map.of();
        }
        Map<String, Region> parentMap = new HashMap<>();
        List<Region> parents = regionService.list(new LambdaQueryWrapper<Region>().eq(Region::getLevel, parentLevel));
        for (Region parent : parents) {
            if (parentMap.put(parent.getCode(), parent) != null) {
                throw new IllegalStateException("sys_region 存在重复区域编码：" + parent.getCode());
            }
        }
        if (parentMap.isEmpty()) {
            throw new IllegalStateException("未找到层级为 " + parentLevel + " 的上级区域");
        }
        return parentMap;
    }

    private Region toRegion(
                            JsonNode node,
                            String resourceName,
                            int recordNumber,
                            RegionLevel level,
                            Map<String, Region> parentMap,
                            String parentCodeField) {
        String code = requiredText(node, "code", resourceName, recordNumber);
        String name = requiredText(node, "name", resourceName, recordNumber);
        Region parent = null;
        if (parentCodeField != null) {
            String parentCode = requiredText(node, parentCodeField, resourceName, recordNumber);
            parent = parentMap.get(parentCode);
            if (parent == null) {
                throw invalidResource(resourceName, recordNumber, "未查找到上级区域：" + parentCode);
            }
        }

        Region region = new Region();
        region.setName(name);
        region.setFullName(parent == null ? name : parent.getFullName() + "/" + name);
        region.setShortName(name);
        region.setPid(parent == null ? null : parent.getId());
        region.setCode(code);
        region.setPath(parent == null ? code : parent.getPath() + "/" + code);
        region.setLevel(level);
        region.setStatus(Boolean.TRUE);
        region.setSort(0);
        region.setCreatedBy(SEED_ACTOR);
        region.setCreatedAt(SEED_TIMESTAMP);
        region.setUpdatedBy(SEED_ACTOR);
        region.setUpdatedAt(SEED_TIMESTAMP);
        return region;
    }

    private String requiredText(JsonNode node, String fieldName, String resourceName, int recordNumber) {
        JsonNode field = node.get(fieldName);
        if (field == null || !field.isString() || field.stringValue().isBlank()) {
            throw invalidResource(resourceName, recordNumber, "字段 " + fieldName + " 必须是非空字符串");
        }
        return field.stringValue();
    }

    private void saveBatch(List<Region> batch, String resourceName, int firstRecord, int lastRecord) {
        if (batch.isEmpty()) {
            return;
        }
        if (!regionService.saveBatch(batch, BATCH_SIZE)) {
            throw new IllegalStateException(
                    "区域资源 " + resourceName + " 写入失败，记录范围：" + firstRecord + "-" + lastRecord);
        }
        batch.clear();
    }

    private ClassPathResource resource(String resourceName) {
        ClassPathResource resource = new ClassPathResource(resourceName);
        if (!resource.exists()) {
            throw new IllegalStateException("找不到区域资源：" + resourceName);
        }
        return resource;
    }

    private void requireArrayStart(JsonParser parser, String resourceName) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("区域资源 " + resourceName + " 的根节点必须是 JSON 数组");
        }
    }

    private void requireEndOfDocument(JsonParser parser, String resourceName) throws IOException {
        if (parser.nextToken() != null) {
            throw new IllegalStateException("区域资源 " + resourceName + " 的 JSON 数组后存在额外内容");
        }
    }

    private IllegalStateException invalidResource(String resourceName, int recordNumber, String reason) {
        return new IllegalStateException("区域资源 " + resourceName + " 第 " + recordNumber + " 条记录无效：" + reason);
    }

}
