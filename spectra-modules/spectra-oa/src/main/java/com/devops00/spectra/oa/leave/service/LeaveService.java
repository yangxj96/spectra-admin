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

/**
 * 请假业务闭环服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public interface LeaveService {
    /**
     * 创建请假申请。
     */
    UUID create(LeaveCreateFrom from);

    /**
     * 修改请假申请。
     */
    void update(UUID id, LeaveCreateFrom from);

    /**
     * 分页查询请假申请。
     */
    IPage<LeaveVO> page(PageFrom page, LeavePageFrom params);

    /**
     * 查询请假申请详情。
     */
    LeaveVO get(UUID id);

    /**
     * 提交请假申请。
     */
    void submit(UUID id, LeaveSubmitFrom from);

    /**
     * 撤回请假申请。
     */
    void withdraw(UUID id);

    /**
     * 取消请假申请。
     */
    void cancel(UUID id);

    /**
     * 处理请假审批通过回调。
     */
    void onApproved(String businessKey, Map<String, Object> variables);

    /**
     * 处理请假审批驳回回调。
     */
    void onRejected(String businessKey, String reason);

    /**
     * 处理请假审批终止回调。
     */
    void onTerminated(String businessKey, String reason);
}
