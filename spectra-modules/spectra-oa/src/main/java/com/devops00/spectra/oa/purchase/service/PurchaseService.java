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

package com.devops00.spectra.oa.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.purchase.javabean.entity.Purchase;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseExecuteFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchasePageFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseReceiptFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseSaveFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseSubmitFrom;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseVO;

import java.util.Map;
import java.util.UUID;

/**
 * 采购申请业务服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
public interface PurchaseService extends BaseService<Purchase> {
    /**
     * 分页查询采购申请。
     */
    IPage<PurchaseVO> page(PageFrom page, PurchasePageFrom params);

    /**
     * 查询采购申请详情。
     */
    PurchaseVO get(UUID id);

    /**
     * 创建采购申请。
     */
    UUID created(PurchaseSaveFrom from);

    /**
     * 修改采购申请。
     */
    void modify(UUID id, PurchaseSaveFrom from);

    /**
     * 提交采购申请。
     */
    void submit(UUID id, PurchaseSubmitFrom from);

    /**
     * 撤回采购申请。
     */
    void withdraw(UUID id);

    /**
     * 取消采购申请。
     */
    void cancel(UUID id);

    /**
     * 执行采购申请。
     */
    void execute(UUID id, PurchaseExecuteFrom from);

    /**
     * 登记采购收货。
     */
    void receive(UUID id, PurchaseReceiptFrom from);

    /**
     * 处理采购审批通过回调。
     */
    void onApproved(String businessKey, Map<String, Object> variables);

    /**
     * 处理采购审批驳回回调。
     */
    void onRejected(String businessKey, String reason);

    /**
     * 处理采购审批终止回调。
     */
    void onTerminated(String businessKey, String reason);
}
