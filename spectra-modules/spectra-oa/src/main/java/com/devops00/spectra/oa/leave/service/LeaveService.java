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

package com.devops00.spectra.oa.leave.service;

import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeaveCreateFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeavePageFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeaveSubmitFrom;
import com.devops00.spectra.oa.leave.javabean.vo.LeaveVO;

/// 请假业务闭环服务。
public interface LeaveService {
    UUID create(LeaveCreateFrom from);

    void update(UUID id, LeaveCreateFrom from);

    IPage<LeaveVO> page(PageFrom page, LeavePageFrom params);

    LeaveVO get(UUID id);

    void submit(UUID id, LeaveSubmitFrom from);

    void withdraw(UUID id);

    void cancel(UUID id);

    void onApproved(String businessKey, Map<String, Object> variables);

    void onRejected(String businessKey, String reason);

    void onTerminated(String businessKey, String reason);
}
