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

import java.util.List;
import java.util.UUID;

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
import com.devops00.spectra.oa.contract.javabean.vo.ContractVersionVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVO;

/// 合同表主表-服务
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 11:53
public interface ContractService extends BaseService<Contract> {

    IPage<ContractVO> page(PageFrom page, ContractPageFrom params);

    ContractVO get(UUID id);

    UUID created(ContractSaveFrom from);

    void modify(UUID id, ContractSaveFrom from);

    void deleteById(UUID id);

    UUID addVersion(UUID id, ContractVersionFrom from);

    List<ContractVersionVO> versions(UUID id);

    UUID createMilestone(UUID id, ContractMilestoneSaveFrom from);

    List<ContractMilestoneVO> milestones(UUID id);

    void updateMilestone(UUID id, UUID milestoneId, ContractMilestoneUpdateFrom from);

    void sign(UUID id);

    void activate(UUID id);

    void terminate(UUID id);

    void archive(UUID id);

    /// 扫描临近到期的履约节点并发送一次性提醒。
    int sendDueMilestoneReminders();

    void preview(UUID id, UUID versionId);

    void download(UUID id, UUID versionId);
}
