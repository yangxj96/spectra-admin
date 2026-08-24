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

package com.devops00.spectra.oa.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseExecuteFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchasePageFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseReceiptFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseSaveFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseSubmitFrom;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseVO;
import com.devops00.spectra.oa.purchase.service.PurchaseService;
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

import java.util.UUID;

/**
 * 采购申请接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@RestController
@RequestMapping("/oa/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * 创建采购申请草稿。
     */
    @ULog("'创建采购申请草稿'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:create')")
    public UUID create(@Validated(Verify.Insert.class) @RequestBody PurchaseSaveFrom from) {
        return purchaseService.created(from);
    }

    /**
     * 修改采购申请草稿。
     */
    @ULog("'修改采购申请草稿'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void update(@PathVariable UUID id, @Validated(Verify.Update.class) @RequestBody PurchaseSaveFrom from) {
        purchaseService.modify(id, from);
    }

    /**
     * 分页查询采购申请。
     */
    @ULog("'分页查询采购申请'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:read')")
    public IPage<PurchaseVO> page(PageFrom page, PurchasePageFrom params) {
        return purchaseService.page(page, params);
    }

    /**
     * 查询采购申请详情。
     */
    @ULog("'查询采购申请详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:read')")
    public PurchaseVO get(@PathVariable UUID id) {
        return purchaseService.get(id);
    }

    /**
     * 提交采购申请审批。
     */
    @ULog("'提交采购申请审批'")
    @PostMapping(value = "/{id}/submit", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void submit(@PathVariable UUID id, @RequestBody(required = false) PurchaseSubmitFrom from) {
        purchaseService.submit(id, from);
    }

    /**
     * 撤回采购申请。
     */
    @ULog("'撤回采购申请'")
    @PostMapping(value = "/{id}/withdraw", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void withdraw(@PathVariable UUID id) {
        purchaseService.withdraw(id);
    }

    /**
     * 取消采购申请。
     */
    @ULog("'取消采购申请'")
    @PostMapping(value = "/{id}/cancel", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:update')")
    public void cancel(@PathVariable UUID id) {
        purchaseService.cancel(id);
    }

    /**
     * 登记采购执行。
     */
    @ULog("'登记采购执行'")
    @PostMapping(value = "/{id}/execute", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:execute')")
    public void execute(@PathVariable UUID id, @RequestBody PurchaseExecuteFrom from) {
        purchaseService.execute(id, from);
    }

    /**
     * 登记采购收货。
     */
    @ULog("'登记采购收货'")
    @PostMapping(value = "/{id}/receipts", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:purchase:receive')")
    public void receive(@PathVariable UUID id, @Validated(Verify.Insert.class) @RequestBody PurchaseReceiptFrom from) {
        purchaseService.receive(id, from);
    }
}
