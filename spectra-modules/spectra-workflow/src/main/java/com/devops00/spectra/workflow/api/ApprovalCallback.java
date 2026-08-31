/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.workflow.api;

import java.util.Map;

/** 审批结果回调端口，由业务模块实现。 */
public interface ApprovalCallback {

    void onApproved(String businessKey, Map<String, Object> variables);

    void onRejected(String businessKey, String reason);

    void onTerminated(String businessKey, String reason);
}
