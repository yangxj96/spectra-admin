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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.system.javabean.entity.RegionPathRow;
import com.devops00.spectra.core.system.javabean.entity.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * 行政区域Mapper
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/1/30 11:49
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {

    /**
     * 一次查询目标区域及其全部父级路径。
     *
     * @param id       目标区域 ID
     * @param maxDepth 最大递归深度
     * @return 从目标区域到根节点的路径行
     */
    List<RegionPathRow> selectPath(@Param("id") UUID id, @Param("maxDepth") int maxDepth);
}
