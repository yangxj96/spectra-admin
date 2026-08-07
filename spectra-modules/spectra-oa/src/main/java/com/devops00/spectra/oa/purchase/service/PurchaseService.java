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

import java.util.Map;
import java.util.UUID;

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

/// 采购申请业务服务。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
public interface PurchaseService extends BaseService<Purchase> {
    IPage<PurchaseVO> page(PageFrom page, PurchasePageFrom params);

    PurchaseVO get(UUID id);

    UUID created(PurchaseSaveFrom from);

    void modify(UUID id, PurchaseSaveFrom from);

    void submit(UUID id, PurchaseSubmitFrom from);

    void withdraw(UUID id);

    void cancel(UUID id);

    void execute(UUID id, PurchaseExecuteFrom from);

    void receive(UUID id, PurchaseReceiptFrom from);

    void onApproved(String businessKey, Map<String, Object> variables);

    void onRejected(String businessKey, String reason);

    void onTerminated(String businessKey, String reason);
}
