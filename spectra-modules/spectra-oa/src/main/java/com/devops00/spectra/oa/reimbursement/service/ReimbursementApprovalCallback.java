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

import com.devops00.spectra.workflow.service.ApprovalCallback;
import com.devops00.spectra.workflow.service.WorkflowService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 报销流程审批回调注册器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Component
@RequiredArgsConstructor
public class ReimbursementApprovalCallback implements ApprovalCallback {

    public static final String PROCESS_DEFINITION_KEY = "oa_reimbursement_approval";

    private final WorkflowService workflowService;
    private final ReimbursementService reimbursementService;

    /**
     * 执行内部处理逻辑（{@code register}）。
     */
    @PostConstruct
    public void register() {
        workflowService.registerCallback(PROCESS_DEFINITION_KEY, this);
    }

    @Override
    public void onApproved(String businessKey, Map<String, Object> variables) {
        reimbursementService.onApproved(businessKey, variables);
    }

    @Override
    public void onRejected(String businessKey, String reason) {
        reimbursementService.onRejected(businessKey, reason);
    }

    @Override
    public void onTerminated(String businessKey, String reason) {
        reimbursementService.onTerminated(businessKey, reason);
    }
}
