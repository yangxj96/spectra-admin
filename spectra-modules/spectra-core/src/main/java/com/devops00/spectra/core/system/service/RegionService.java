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

package com.devops00.spectra.core.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.system.javabean.entity.Region;
import com.devops00.spectra.core.system.javabean.from.RegionFrom;
import com.devops00.spectra.core.system.javabean.from.RegionPageFrom;
import com.devops00.spectra.core.system.javabean.vo.RegionPathVO;
import com.devops00.spectra.core.system.javabean.vo.RegionVO;

import java.util.List;
import java.util.UUID;

/**
 * 行政区域Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/1/30 13:57
 */
public interface RegionService extends BaseService<Region> {

    /**
     * 懒加载树
     *
     * @param level 层级
     * @param id    父级ID
     * @return 根据条件获取的下级的列表
     */
    List<RegionVO> lazyTree(Integer level, String id);

    /**
     * 分页查询行政区划
     *
     * @param page   分页信息
     * @param params 过滤参数
     * @return 分页响应信息
     */
    IPage<RegionVO> page(PageFrom page, RegionPageFrom params);

    /**
     * 查询或获取目标数据（{@code getPath}）。
     */
    RegionPathVO getPath(UUID id);

    /**
     * 新增行政区划
     *
     * @param params 行政区划信息
     * @return 新增后的行政区划信息
     */
    RegionVO created(RegionFrom params);

    /**
     * 修改行政区划
     *
     * @param params 行政区划信息
     * @return 修改后的行政区划信息
     */
    RegionVO modify(RegionFrom params);

    /**
     * 根据ID删除行政区划
     *
     * @param id 行政区划ID
     */
    void deleteById(UUID id);
}
