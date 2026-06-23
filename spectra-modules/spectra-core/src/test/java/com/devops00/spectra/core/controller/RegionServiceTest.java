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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/// 区域测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/1/30 14:01
@Slf4j
@SpringBootTest
public class RegionServiceTest {

    @Resource
    private RegionService regionService;

    @Resource
    private ObjectMapper om;

    @Test
    void importProvinces() throws IOException {
        // 读取 test/resources 下的文件
        var resource = new ClassPathResource("regions/provinces.json");
        var root = om.readTree(resource.getInputStream());
        if (root != null && root.isArray()) {
            var regions = new ArrayList<Region>();
            for (JsonNode node : root) {
                var datum = new Region();
                datum.setName(node.get("name").stringValue());
                datum.setFullName(node.get("name").stringValue());
                datum.setShortName(node.get("name").stringValue());

                datum.setCode(node.get("code").stringValue());
                datum.setPath(node.get("code").stringValue());
                datum.setLevel(RegionLevel.PROVINCES);
                datum.setStatus(Boolean.TRUE);
                datum.setSort(0);

                regions.add(datum);
            }

            regionService.saveBatch(regions, 500);
        }
    }


    @Test
    void importCities() throws IOException {
        var regionMap = regionService
                .list(
                        new LambdaQueryWrapper<Region>()
                                .eq(Region::getLevel, RegionLevel.PROVINCES)
                ).stream()
                .collect(Collectors.toMap(Region::getCode, e -> e));

        // 读取 test/resources 下的文件
        var resource = new ClassPathResource("regions/cities.json");
        var root = om.readTree(resource.getInputStream());
        if (root != null && root.isArray()) {
            var regions = new ArrayList<Region>();
            for (JsonNode node : root) {
                var provinceCode = node.get("provinceCode").stringValue();
                Region region = regionMap.get(provinceCode);
                if (region == null) {
                    throw new RuntimeException("未查找到上级");
                }

                var datum = new Region();
                datum.setName(node.get("name").stringValue());
                datum.setFullName(region.getFullName() + "/" + node.get("name").stringValue());
                datum.setShortName(node.get("name").stringValue());
                datum.setPid(region.getId());
                datum.setCode(node.get("code").stringValue());
                datum.setPath(region.getPath() + "/" + node.get("code").stringValue());
                datum.setLevel(RegionLevel.CITIES);
                datum.setStatus(Boolean.TRUE);
                datum.setSort(0);

                regions.add(datum);

            }
            regionService.saveBatch(regions, 500);
        }
    }

    @Test
    void importAreas() throws IOException {
        var regionMap = regionService
                .list(
                        new LambdaQueryWrapper<Region>()
                                .eq(Region::getLevel, RegionLevel.CITIES)
                ).stream()
                .collect(Collectors.toMap(Region::getCode, e -> e));

        // 读取 test/resources 下的文件
        var resource = new ClassPathResource("regions/areas.json");
        var root = om.readTree(resource.getInputStream());
        if (root != null && root.isArray()) {
            var regions = new ArrayList<Region>();
            for (JsonNode node : root) {
                var cityCode = node.get("cityCode").stringValue();
                Region region = regionMap.get(cityCode);
                if (region == null) {
                    throw new RuntimeException("未查找到上级");
                }

                var datum = new Region();
                datum.setName(node.get("name").stringValue());
                datum.setFullName(region.getFullName() + "/" + node.get("name").stringValue());
                datum.setShortName(node.get("name").stringValue());
                datum.setPid(region.getId());
                datum.setCode(node.get("code").stringValue());
                datum.setPath(region.getPath() + "/" + node.get("code").stringValue());
                datum.setLevel(RegionLevel.AREAS);
                datum.setStatus(Boolean.TRUE);
                datum.setSort(0);

                regions.add(datum);
            }
            regionService.saveBatch(regions, 500);
        }
    }

    @Test
    void importStreets() throws IOException {
        var regionMap = regionService
                .list(
                        new LambdaQueryWrapper<Region>()
                                .eq(Region::getLevel, RegionLevel.AREAS)
                ).stream()
                .collect(Collectors.toMap(Region::getCode, e -> e));

        // 读取 test/resources 下的文件
        var resource = new ClassPathResource("regions/streets.json");
        var root = om.readTree(resource.getInputStream());
        if (root != null && root.isArray()) {
            var regions = new ArrayList<Region>();
            for (JsonNode node : root) {
                var areaCode = node.get("areaCode").stringValue();
                Region region = regionMap.get(areaCode);
                if (region == null) {
                    throw new RuntimeException("未查找到上级");
                }

                var datum = new Region();
                datum.setName(node.get("name").stringValue());
                datum.setFullName(region.getFullName() + "/" + node.get("name").stringValue());
                datum.setShortName(node.get("name").stringValue());
                datum.setPid(region.getId());
                datum.setCode(node.get("code").stringValue());
                datum.setPath(region.getPath() + "/" + node.get("code").stringValue());
                datum.setLevel(RegionLevel.STREETS);
                datum.setStatus(Boolean.TRUE);
                datum.setSort(0);

                regions.add(datum);
            }
            regionService.saveBatch(regions, 500);
        }
    }

    @Test
    void importVillages() throws IOException {
        var regionMap = regionService
                .list(
                        new LambdaQueryWrapper<Region>()
                                .eq(Region::getLevel, RegionLevel.STREETS)
                ).stream()
                .collect(Collectors.toMap(Region::getCode, e -> e));

        // 读取 test/resources 下的文件
        var resource = new ClassPathResource("regions/villages.json");
        var root = om.readTree(resource.getInputStream());
        if (root != null && root.isArray()) {
            var regions = new ArrayList<Region>();
            for (JsonNode node : root) {
                var streetCode = node.get("streetCode").stringValue();
                Region region = regionMap.get(streetCode);
                if (region == null) {
                    throw new RuntimeException("未查找到上级");
                }

                var datum = new Region();
                datum.setName(node.get("name").stringValue());
                datum.setFullName(region.getFullName() + "/" + node.get("name").stringValue());
                datum.setShortName(node.get("name").stringValue());
                datum.setPid(region.getId());
                datum.setCode(node.get("code").stringValue());
                datum.setPath(region.getPath() + "/" + node.get("code").stringValue());
                datum.setLevel(RegionLevel.VILLAGES);
                datum.setStatus(Boolean.TRUE);
                datum.setSort(0);

                regions.add(datum);
            }
            regionService.saveBatch(regions, 500);
        }
    }

}
