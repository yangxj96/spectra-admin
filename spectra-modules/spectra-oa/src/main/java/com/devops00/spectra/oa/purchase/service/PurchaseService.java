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
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 采购状态、执行状态和关键字等采购申请分页筛选条件。
     * @return 返回按分页条件查询的OA 采购申请分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<PurchaseVO> page(PageFrom page, PurchasePageFrom params);

    /**
     * 查询采购申请详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 采购申请详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    PurchaseVO get(UUID id);

    /**
     * 创建采购申请。
     *
     * @param from 采购用途、期望日期、预算、供应商和采购明细等申请字段。
     * @return 返回新建采购申请的唯一标识；采购字段校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID created(PurchaseSaveFrom from);

    /**
     * 修改采购申请。
     *
     * @param id   待修改采购申请的唯一标识。
     * @param from 采购用途、期望日期、预算、供应商和采购明细等修改字段。
     */
    void modify(UUID id, PurchaseSaveFrom from);

    /**
     * 提交采购申请。
     *
     * @param id   待提交采购申请的唯一标识。
     * @param from 审批人邮箱，用于确定本次采购申请的审批通知对象。
     */
    void submit(UUID id, PurchaseSubmitFrom from);

    /**
     * 撤回采购申请。
     *
     * @param id 待撤回采购申请的唯一标识；仅允许撤回尚未完成审批的申请。
     */
    void withdraw(UUID id);

    /**
     * 取消采购申请。
     *
     * @param id 待取消采购申请的唯一标识。
     */
    void cancel(UUID id);

    /**
     * 执行采购申请。
     *
     * @param id   待执行采购申请的唯一标识。
     * @param from 采购人、订单号、执行状态和执行备注等采购执行字段。
     */
    void execute(UUID id, PurchaseExecuteFrom from);

    /**
     * 登记采购收货。
     *
     * @param id   待登记收货的采购申请唯一标识。
     * @param from 收货单号、收货日期、收货人、收货明细和差异说明。
     */
    void receive(UUID id, PurchaseReceiptFrom from);

    /**
     * 处理采购审批通过回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param variables   流程启动或任务处理时使用的流程变量。
     */
    void onApproved(String businessKey, Map<String, Object> variables);

    /**
     * 处理采购审批驳回回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param reason      本次状态变更或撤销的业务原因。
     */
    void onRejected(String businessKey, String reason);

    /**
     * 处理采购审批终止回调。
     *
     * @param businessKey 流程关联的业务键，用于定位业务单据。
     * @param reason      本次状态变更或撤销的业务原因。
     */
    void onTerminated(String businessKey, String reason);
}
