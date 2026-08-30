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
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
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
     */
    IPage<ContractVO> page(PageFrom page, ContractPageFrom params);

    /**
     * 查询合同详情。
     */
    ContractVO get(UUID id);

    /**
     * 创建合同。
     */
    UUID created(ContractSaveFrom from);

    /**
     * 修改合同。
     */
    void modify(UUID id, ContractSaveFrom from);

    /**
     * 删除合同。
     */
    void deleteById(UUID id);

    /**
     * 新增合同版本。
     */
    UUID addVersion(UUID id, ContractVersionFrom from);

    /**
     * 查询合同版本列表。
     */
    List<ContractVersionVO> versions(UUID id);

    /**
     * 创建合同履约节点。
     */
    UUID createMilestone(UUID id, ContractMilestoneSaveFrom from);

    /**
     * 查询合同履约节点。
     */
    List<ContractMilestoneVO> milestones(UUID id);

    /**
     * 修改合同履约节点。
     */
    void updateMilestone(UUID id, UUID milestoneId, ContractMilestoneUpdateFrom from);

    /**
     * 签署合同。
     */
    void sign(UUID id);

    /**
     * 激活合同。
     */
    void activate(UUID id);

    /**
     * 终止合同。
     */
    void terminate(UUID id);

    /**
     * 归档合同。
     */
    void archive(UUID id);

    /**
     * 扫描临近到期的履约节点并发送一次性提醒。
     */
    int sendDueMilestoneReminders();

}
