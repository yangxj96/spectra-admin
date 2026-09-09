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

package com.devops00.spectra.oa.contract.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.base.BaseService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.javabean.from.ContractMilestoneSaveFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractMilestoneUpdateFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractPageFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractSaveFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractVersionFrom;
import com.devops00.spectra.oa.contract.javabean.vo.ContractMilestoneVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVersionVO;

import java.util.List;
import java.util.UUID;

/**
 * 合同表主表-服务
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 11:53
 */
public interface ContractService extends BaseService<Contract> {

    /**
     * 分页查询合同。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 合同关键字、合同类型、签署状态和生命周期状态等分页筛选条件。
     * @return 返回按分页条件查询的OA 合同分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<ContractVO> page(PageFrom page, ContractPageFrom params);

    /**
     * 查询合同详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 合同详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    ContractVO get(UUID id);

    /**
     * 创建合同。
     *
     * @param from 合同标题、类型、相对方、金额、币种、起止日期和可见范围等创建字段。
     * @return 返回新建合同的唯一标识，供合同履约、签署和归档操作定位合同；校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID created(ContractSaveFrom from);

    /**
     * 修改合同。
     *
     * @param id   待修改合同的唯一标识。
     * @param from 合同标题、类型、相对方、金额、币种、起止日期和可见范围等修改字段。
     */
    void modify(UUID id, ContractSaveFrom from);

    /**
     * 删除合同。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void deleteById(UUID id);

    /**
     * 新增合同版本。
     *
     * @param id   要新增版本的合同唯一标识。
     * @param from 新版本文件资产、文件名、大小、媒体类型和版本说明。
     * @return 返回新建合同版本的唯一标识，供版本查询和合同内容追踪定位该版本；校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID addVersion(UUID id, ContractVersionFrom from);

    /**
     * 查询合同版本列表。
     *
     * @param id 要查询版本列表的合同唯一标识。
     * @return 返回符合查询条件的OA 合同版本列表；无匹配时返回空列表，不返回 null。
     */
    List<ContractVersionVO> versions(UUID id);

    /**
     * 创建合同履约节点。
     *
     * @param id   要新增履约节点的合同唯一标识。
     * @param from 节点名称、节点类型、到期日、负责人和备注等履约字段。
     * @return 返回新建合同履约节点的唯一标识，供里程碑更新和到期提醒定位节点；校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID createMilestone(UUID id, ContractMilestoneSaveFrom from);

    /**
     * 查询合同履约节点。
     *
     * @param id 要查询履约节点的合同唯一标识。
     * @return 返回符合查询条件的OA 合同里程碑列表；无匹配时返回空列表，不返回 null。
     */
    List<ContractMilestoneVO> milestones(UUID id);

    /**
     * 修改合同履约节点。
     *
     * @param id          包含该履约节点的合同唯一标识。
     * @param milestoneId 待更新履约节点的唯一标识。
     * @param from        节点状态、完成时间和备注等履约更新字段。
     */
    void updateMilestone(UUID id, UUID milestoneId, ContractMilestoneUpdateFrom from);

    /**
     * 签署合同。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void sign(UUID id);

    /**
     * 激活合同。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void activate(UUID id);

    /**
     * 终止合同。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void terminate(UUID id);

    /**
     * 归档合同。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void archive(UUID id);

    /**
     * 扫描临近到期的履约节点并发送一次性提醒。
     *
     * @return 返回本次扫描并成功发送提醒的合同履约节点数量；没有临近到期节点时返回 0，发送或查询失败时抛出异常。
     */
    int sendDueMilestoneReminders();

}
