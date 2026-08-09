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

package com.devops00.spectra.oa.contract.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.contract.javabean.from.ContractMilestoneSaveFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractMilestoneUpdateFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractPageFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractSaveFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractVersionFrom;
import com.devops00.spectra.oa.contract.javabean.vo.ContractMilestoneVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVersionVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVO;
import com.devops00.spectra.oa.contract.service.ContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 合同台账接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Slf4j
@RestController
@RequestMapping("/oa/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /**
     * 分页查询合同。
     */
    @ULog("'分页查询合同'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:QUERY')")
    public IPage<ContractVO> page(PageFrom page, ContractPageFrom params) {
        return contractService.page(page, params);
    }

    /**
     * 查询合同详情。
     */
    @ULog("'查询合同详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:QUERY')")
    public ContractVO get(@PathVariable UUID id) {
        return contractService.get(id);
    }

    /**
     * 创建合同台账。
     */
    @ULog("'创建合同台账'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:INSERT')")
    public UUID created(@Validated @RequestBody ContractSaveFrom from) {
        return contractService.created(from);
    }

    /**
     * 修改合同台账。
     */
    @ULog("'修改合同台账'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public void modify(@PathVariable UUID id, @Validated @RequestBody ContractSaveFrom from) {
        contractService.modify(id, from);
    }

    /**
     * 删除合同台账。
     */
    @ULog("'删除合同台账'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:DELETE')")
    public void deleteById(@PathVariable UUID id) {
        contractService.deleteById(id);
    }

    /**
     * 新增合同版本。
     */
    @ULog("'新增合同版本'")
    @PostMapping(value = "/{id}/versions", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public UUID addVersion(@PathVariable UUID id, @Validated @RequestBody ContractVersionFrom from) {
        return contractService.addVersion(id, from);
    }

    /**
     * 查询合同版本。
     */
    @ULog("'查询合同版本'")
    @GetMapping(value = "/{id}/versions", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:QUERY')")
    public List<ContractVersionVO> versions(@PathVariable UUID id) {
        return contractService.versions(id);
    }

    /**
     * 创建合同履约节点。
     */
    @ULog("'创建合同履约节点'")
    @PostMapping(value = "/{id}/milestones", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public UUID createMilestone(@PathVariable UUID id, @Validated @RequestBody ContractMilestoneSaveFrom from) {
        return contractService.createMilestone(id, from);
    }

    /**
     * 查询合同履约节点。
     */
    @ULog("'查询合同履约节点'")
    @GetMapping(value = "/{id}/milestones", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:QUERY')")
    public List<ContractMilestoneVO> milestones(@PathVariable UUID id) {
        return contractService.milestones(id);
    }

    /**
     * 更新合同履约节点。
     */
    @ULog("'更新合同履约节点'")
    @PutMapping(value = "/{id}/milestones/{milestoneId}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public void updateMilestone(@PathVariable UUID id, @PathVariable UUID milestoneId, @Validated @RequestBody ContractMilestoneUpdateFrom from) {
        contractService.updateMilestone(id, milestoneId, from);
    }

    /**
     * 标记合同已签署。
     */
    @ULog("'标记合同已签署'")
    @PostMapping(value = "/{id}/sign", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public void sign(@PathVariable UUID id) {
        contractService.sign(id);
    }

    /**
     * 启用合同。
     */
    @ULog("'启用合同'")
    @PostMapping(value = "/{id}/activate", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public void activate(@PathVariable UUID id) {
        contractService.activate(id);
    }

    /**
     * 终止合同。
     */
    @ULog("'终止合同'")
    @PostMapping(value = "/{id}/terminate", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public void terminate(@PathVariable UUID id) {
        contractService.terminate(id);
    }

    /**
     * 归档合同。
     */
    @ULog("'归档合同'")
    @PostMapping(value = "/{id}/archive", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public void archive(@PathVariable UUID id) {
        contractService.archive(id);
    }

    /**
     * 执行合同履约提醒扫描。
     */
    @ULog("'执行合同履约提醒扫描'")
    @PostMapping(value = "/reminders/run", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:UPDATE')")
    public int runReminders() {
        return contractService.sendDueMilestoneReminders();
    }

    /**
     * 预览合同版本。
     */
    @ULog("'预览合同版本'")
    @GetMapping(value = "/{id}/versions/{versionId}/preview", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:QUERY')")
    public void preview(@PathVariable UUID id, @PathVariable UUID versionId) {
        contractService.preview(id, versionId);
    }

    /**
     * 下载合同版本。
     */
    @ULog("'下载合同版本'")
    @GetMapping(value = "/{id}/versions/{versionId}/download", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_CONTRACT:QUERY')")
    public void download(@PathVariable UUID id, @PathVariable UUID versionId) {
        contractService.download(id, versionId);
    }
}
