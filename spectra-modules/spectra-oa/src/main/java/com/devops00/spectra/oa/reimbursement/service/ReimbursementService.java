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

package com.devops00.spectra.oa.reimbursement.service;

import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.reimbursement.javabean.entity.Reimbursement;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPageFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPaymentFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSaveFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSubmitFrom;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementVO;

/// 费用报销业务服务。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
public interface ReimbursementService extends BaseService<Reimbursement> {
    IPage<ReimbursementVO> page(PageFrom page, ReimbursementPageFrom params);

    ReimbursementVO get(UUID id);

    UUID created(ReimbursementSaveFrom from);

    void modify(UUID id, ReimbursementSaveFrom from);

    void submit(UUID id, ReimbursementSubmitFrom from);

    void withdraw(UUID id);

    void cancel(UUID id);

    void markPaid(UUID id, ReimbursementPaymentFrom from);

    void onApproved(String businessKey, Map<String, Object> variables);

    void onRejected(String businessKey, String reason);

    void onTerminated(String businessKey, String reason);
}
