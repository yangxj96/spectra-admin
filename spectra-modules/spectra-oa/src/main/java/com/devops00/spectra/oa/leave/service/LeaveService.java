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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeaveCreateFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeavePageFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeaveSubmitFrom;
import com.devops00.spectra.oa.leave.javabean.vo.LeaveVO;

import java.util.Map;
import java.util.UUID;

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
     *
     * @param from 请假类型、起止时间、请假事由、联系地址和时长计算选项等申请字段。
     * @return 返回新建请假申请的唯一标识；请假时间或审批信息校验失败时抛出业务异常，不返回 null。
     */
    UUID create(LeaveCreateFrom from);

    /**
     * 修改请假申请。
     *
     * @param id   待修改请假申请的唯一标识。
     * @param from 请假类型、起止时间、请假事由、联系地址和时长计算选项等修改字段。
     */
    void update(UUID id, LeaveCreateFrom from);

    /**
     * 分页查询请假申请。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 请假状态和请假类型等请假申请分页筛选条件。
     * @return 返回按分页条件查询的OA 请假申请分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<LeaveVO> page(PageFrom page, LeavePageFrom params);

    /**
     * 查询请假申请详情。
     *
     * @param id 待提交请假申请的唯一标识。
     * @return 返回符合条件的OA 请假申请详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    LeaveVO get(UUID id);

    /**
     * 提交请假申请。
     *
     * @param id   目标OA 业务记录的唯一标识。
     * @param from 审批人邮箱，用于确定本次请假申请的审批通知对象。
     */
    void submit(UUID id, LeaveSubmitFrom from);

    /**
     * 撤回请假申请。
     *
     * @param id 待撤回请假申请的唯一标识；仅允许撤回尚未完成审批的申请。
     */
    void withdraw(UUID id);

    /**
     * 取消请假申请。
     *
     * @param id 待取消请假申请的唯一标识。
     */
    void cancel(UUID id);

    /**
     * 处理请假审批通过回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param variables   流程启动或任务处理时使用的流程变量。
     */
    void onApproved(String businessKey, Map<String, Object> variables);

    /**
     * 处理请假审批驳回回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param reason      本次状态变更或撤销的业务原因。
     */
    void onRejected(String businessKey, String reason);

    /**
     * 处理请假审批终止回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param reason      本次状态变更或撤销的业务原因。
     */
    void onTerminated(String businessKey, String reason);
}
