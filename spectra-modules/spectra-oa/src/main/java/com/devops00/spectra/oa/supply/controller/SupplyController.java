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

package com.devops00.spectra.oa.supply.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.oa.supply.javabean.from.SupplyOperationFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyPageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;
import com.devops00.spectra.oa.supply.service.SupplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 办公用品库存接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@RestController
@RequestMapping("/oa/supplies")
@RequiredArgsConstructor
@Slf4j
public class SupplyController {
    private final SupplyService supplyService;

    /**
     * 创建办公用品。
     */
    @Audit("'创建办公用品'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:create')")
    public UUID create(@Validated(Verify.Insert.class) @RequestBody SupplySaveFrom from) {
        return supplyService.created(from);
    }

    /**
     * 修改办公用品。
     */
    @Audit("'修改办公用品'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void update(@PathVariable UUID id, @Validated(Verify.Update.class) @RequestBody SupplySaveFrom from) {
        supplyService.modify(id, from);
    }

    /**
     * 分页查询办公用品库存。
     */
    @Audit("'分页查询办公用品库存'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:read')")
    public IPage<SupplyItemVO> page(PageFrom page, SupplyPageFrom params) {
        return supplyService.page(page, params);
    }

    /**
     * 查询办公用品详情。
     */
    @Audit("'查询办公用品详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:read')")
    public SupplyItemVO get(@PathVariable UUID id) {
        return supplyService.get(id);
    }

    /**
     * 查询低库存办公用品。
     */
    @Audit("'查询低库存办公用品'")
    @GetMapping(value = "/low-stock", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:read')")
    public List<SupplyItemVO> lowStock() {
        return supplyService.lowStock();
    }

    /**
     * 办公用品入库。
     */
    @Audit("'办公用品入库'")
    @PostMapping(value = "/{id}/inbound", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void inbound(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.inbound(id, from);
    }

    /**
     * 办公用品领用。
     */
    @Audit("'办公用品领用'")
    @PostMapping(value = "/{id}/issue", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void issue(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.issue(id, from);
    }

    /**
     * 办公用品退库。
     */
    @Audit("'办公用品退库'")
    @PostMapping(value = "/{id}/return", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void returnStock(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.returnStock(id, from);
    }

    /**
     * 办公用品盘点调整。
     */
    @Audit("'办公用品盘点调整'")
    @PostMapping(value = "/{id}/adjust", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void adjust(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.adjust(id, from);
    }
}
