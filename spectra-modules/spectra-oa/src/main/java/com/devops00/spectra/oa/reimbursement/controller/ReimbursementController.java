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

package com.devops00.spectra.oa.reimbursement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPageFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPaymentFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSaveFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSubmitFrom;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementVO;
import com.devops00.spectra.oa.reimbursement.service.ReimbursementService;
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
 * 费用报销接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@RestController
@RequestMapping("/oa/reimbursements")
@RequiredArgsConstructor
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    /**
     * 创建报销草稿。
     */
    @ULog("'创建报销草稿'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:create')")
    public UUID create(@Validated(Verify.Insert.class) @RequestBody ReimbursementSaveFrom from) {
        return reimbursementService.created(from);
    }

    /**
     * 修改报销草稿。
     */
    @ULog("'修改报销草稿'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:update')")
    public void update(@PathVariable UUID id, @Validated(Verify.Update.class) @RequestBody ReimbursementSaveFrom from) {
        reimbursementService.modify(id, from);
    }

    /**
     * 分页查询报销单。
     */
    @ULog("'分页查询报销单'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:read')")
    public IPage<ReimbursementVO> page(PageFrom page, ReimbursementPageFrom params) {
        return reimbursementService.page(page, params);
    }

    /**
     * 查询报销详情。
     */
    @ULog("'查询报销详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:read')")
    public ReimbursementVO get(@PathVariable UUID id) {
        return reimbursementService.get(id);
    }

    /**
     * 提交报销审批。
     */
    @ULog("'提交报销审批'")
    @PostMapping(value = "/{id}/submit", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:update')")
    public void submit(@PathVariable UUID id, @RequestBody(required = false) ReimbursementSubmitFrom from) {
        reimbursementService.submit(id, from);
    }

    /**
     * 撤回报销申请。
     */
    @ULog("'撤回报销申请'")
    @PostMapping(value = "/{id}/withdraw", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:update')")
    public void withdraw(@PathVariable UUID id) {
        reimbursementService.withdraw(id);
    }

    /**
     * 取消报销申请。
     */
    @ULog("'取消报销申请'")
    @PostMapping(value = "/{id}/cancel", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:update')")
    public void cancel(@PathVariable UUID id) {
        reimbursementService.cancel(id);
    }

    /**
     * 登记报销付款。
     */
    @ULog("'登记报销付款'")
    @PostMapping(value = "/{id}/payment", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:reimbursement:pay')")
    public void payment(@PathVariable UUID id, @RequestBody(required = false) ReimbursementPaymentFrom from) {
        reimbursementService.markPaid(id, from);
    }
}
