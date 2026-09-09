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
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
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
     * @param level 要展开的行政区划层级。
     * @param id    父级区域 ID；为空时从顶级区域开始查询。
     * @return 返回指定层级和父级下的行政区划列表；没有下级区域时返回空列表，不返回 null。
     */
    List<RegionVO> lazyTree(Integer level, String id);

    /**
     * 分页查询行政区划
     *
     * @param page   行政区划列表的页码、页大小及排序字段。
     * @param params 区域名称、编码、层级和父级等分页筛选条件。
     * @return 返回按行政区划条件分页查询的区域结果；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<RegionVO> page(PageFrom page, RegionPageFrom params);

    /**
     * 查询或获取目标数据（{@code getPath}）。
     *
     * @param id 要查询组织路径的行政区划唯一标识。
     * @return 返回从指定行政区划到根节点的编码、名称和层级路径；区域不存在时抛出业务异常，不返回 null。
     */
    RegionPathVO getPath(UUID id);

    /**
     * 新增行政区划
     *
     * @param params 行政区划编码、名称、层级、父级和排序等新建字段。
     * @return 返回新增行政区划的视图，包含区域标识、名称和层级；校验或写入失败时抛出业务异常，不返回 null。
     */
    RegionVO created(RegionFrom params);

    /**
     * 修改行政区划
     *
     * @param params 待修改行政区划的唯一标识及编码、名称、层级、父级和排序字段。
     * @return 返回修改后的行政区划视图；区域不存在、层级冲突或写入失败时抛出业务异常，不返回 null。
     */
    RegionVO modify(RegionFrom params);

    /**
     * 根据ID删除行政区划
     *
     * @param id 行政区划ID
     */
    void deleteById(UUID id);
}
