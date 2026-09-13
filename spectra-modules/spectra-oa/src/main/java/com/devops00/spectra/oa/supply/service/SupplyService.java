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

package com.devops00.spectra.oa.supply.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyOperationFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyPageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;

import java.util.List;
import java.util.UUID;

/**
 * 办公用品库存服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public interface SupplyService {
    /**
     * 分页查询办公用品库存。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 物资关键字、分类、状态和低库存标记等库存分页筛选条件。
     * @return 返回按分页条件查询的OA 物资分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<SupplyItemVO> page(PageFrom page, SupplyPageFrom params);

    /**
     * 查询办公用品详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 物资详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    SupplyItemVO get(UUID id);

    /**
     * 创建办公用品 SKU。
     *
     * @param from 物资分类、SKU、名称、规格、单位、最低库存、供应商、位置和所属部门等字段。
     * @return 返回新建物资的唯一标识；物资编码或库存字段校验失败时抛出业务异常，不返回 null。
     */
    UUID created(SupplySaveFrom from);

    /**
     * 修改办公用品 SKU。
     *
     * @param id   待修改物资 SKU 的唯一标识。
     * @param from 物资分类、SKU、名称、规格、单位、最低库存、供应商、位置和所属部门等修改字段。
     */
    void modify(UUID id, SupplySaveFrom from);

    /**
     * 办公用品入库。
     *
     * @param id   待入库物资 SKU 的唯一标识。
     * @param from 入库数量、目标库存、部门、位置、日期、原因及来源采购单信息。
     */
    void inbound(UUID id, SupplyOperationFrom from);

    /**
     * 办公用品领用出库。
     *
     * @param id   待领用出库物资 SKU 的唯一标识。
     * @param from 出库数量、领用部门、领用人、位置、日期和领用原因。
     */
    void issue(UUID id, SupplyOperationFrom from);

    /**
     * 办公用品退库。
     *
     * @param id   待退库物资 SKU 的唯一标识。
     * @param from 退库数量、退回部门、位置、日期和退库原因。
     */
    void returnStock(UUID id, SupplyOperationFrom from);

    /**
     * 调整办公用品库存。
     *
     * @param id   待调整库存物资 SKU 的唯一标识。
     * @param from 调整后的目标库存、调整部门、位置、日期和调整原因。
     */
    void adjust(UUID id, SupplyOperationFrom from);

    /**
     * 查询低库存办公用品。
     *
     * @return 返回符合查询条件的OA 物资列表；无匹配时返回空列表，不返回 null。
     */
    List<SupplyItemVO> lowStock();
}
