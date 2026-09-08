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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.reimbursement.javabean.entity.Reimbursement;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPageFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPaymentFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSaveFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSubmitFrom;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementVO;

import java.util.Map;
import java.util.UUID;

/**
 * 费用报销业务服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
public interface ReimbursementService extends BaseService<Reimbursement> {
    /**
     * 分页查询报销单。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 报销状态、付款状态和关键字等报销单分页筛选条件。
     * @return 返回按分页条件查询的OA 报销申请分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<ReimbursementVO> page(PageFrom page, ReimbursementPageFrom params);

    /**
     * 查询报销单详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 报销申请详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    ReimbursementVO get(UUID id);

    /**
     * 创建报销单。
     *
     * @param from 报销用途、费用起止日期、金额、币种、收款人和费用明细等申请字段。
     * @return 返回新建报销申请的唯一标识；金额、票据或写入校验失败时抛出业务异常，不返回 null。
     */
    UUID created(ReimbursementSaveFrom from);

    /**
     * 修改报销单。
     *
     * @param id   待修改报销单的唯一标识。
     * @param from 报销用途、费用起止日期、金额、币种、收款人和费用明细等修改字段。
     */
    void modify(UUID id, ReimbursementSaveFrom from);

    /**
     * 提交报销单。
     *
     * @param id   待提交报销单的唯一标识。
     * @param from 审批人邮箱，用于确定本次报销申请的审批通知对象。
     */
    void submit(UUID id, ReimbursementSubmitFrom from);

    /**
     * 撤回报销单。
     *
     * @param id 待撤回报销单的唯一标识；仅允许撤回尚未完成审批的报销单。
     */
    void withdraw(UUID id);

    /**
     * 取消报销单。
     *
     * @param id 待取消报销单的唯一标识。
     */
    void cancel(UUID id);

    /**
     * 登记报销付款。
     *
     * @param id   待登记付款的报销单唯一标识。
     * @param from 付款备注，用于记录付款结果或财务处理说明。
     */
    void markPaid(UUID id, ReimbursementPaymentFrom from);

    /**
     * 处理报销审批通过回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param variables   流程启动或任务处理时使用的流程变量。
     */
    void onApproved(String businessKey, Map<String, Object> variables);

    /**
     * 处理报销审批驳回回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param reason      本次状态变更或撤销的业务原因。
     */
    void onRejected(String businessKey, String reason);

    /**
     * 处理报销审批终止回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param reason      本次状态变更或撤销的业务原因。
     */
    void onTerminated(String businessKey, String reason);
}
