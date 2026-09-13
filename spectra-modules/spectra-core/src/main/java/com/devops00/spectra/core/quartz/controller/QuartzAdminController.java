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

package com.devops00.spectra.core.quartz.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.quartz.javabean.from.QuartzHistoryQueryFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobCreateFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobUpdateFrom;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzExecutionHistoryVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobTypeVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzTriggerVO;
import com.devops00.spectra.core.quartz.service.QuartzJobManagementService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.UUID;

/**
 * Quartz Job、Trigger 和执行历史的管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduler/quartz")
public class QuartzAdminController {

    private final QuartzJobManagementService managementService;

    /**
     * 查询代码白名单中的 Job 类型和参数 schema。
     *
     * @return 可选 Job 类型能力列表；无类型时返回空列表而不是 null
     */
    @Audit("'查询 Quartz Job 类型目录'")
    @GetMapping(value = "/job-types", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS', 'ROLE_AUDIT')")
    public List<QuartzJobTypeVO> jobTypes() {
        return managementService.jobTypes();
    }

    /**
     * 分页查询 Quartz Job。
     *
     * @param page 分页页码和页大小；未提供时使用系统默认值
     * @return Job 分页；无匹配时 records 为空且不返回 null
     */
    @Audit("'查询 Quartz Job'")
    @GetMapping(value = "/jobs", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS', 'ROLE_AUDIT')")
    public IPage<QuartzJobVO> jobs(PageFrom page) {
        return managementService.jobs(page);
    }

    /**
     * 查询单个 Quartz Job 及其唯一 Trigger。
     *
     * @param jobKey JobKey 名称或完整 JobKey
     * @return Job 和 Trigger 安全详情；不存在时由统一异常处理返回 404
     */
    @Audit("'查询 Quartz Job 详情'")
    @GetMapping(value = "/jobs/{jobKey}", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS', 'ROLE_AUDIT')")
    public QuartzJobVO job(@PathVariable String jobKey) {
        return managementService.job(jobKey);
    }

    /**
     * 创建一个由代码白名单实现的普通 Quartz Job。
     *
     * @param from Job 展示信息、类型、版本化参数和唯一 Trigger 配置
     * @return 创建完成的 Job；JobKey 由服务端生成，不返回 null
     */
    @Audit("'创建 Quartz Job'")
    @PostMapping(value = "/jobs", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')")
    public QuartzJobVO created(@Valid @Validated @RequestBody QuartzJobCreateFrom from) {
        return managementService.create(from);
    }

    /**
     * 修改同一 JobKey 的普通或受保护 Job 配置。
     *
     * @param jobKey JobKey 名称或完整 JobKey
     * @param from   更新后的展示信息、参数和唯一 Trigger 配置
     * @return 修改完成的 Job；JobKey 和实现类不会被请求替换
     */
    @Audit("'修改 Quartz Job'")
    @PutMapping(value = "/jobs/{jobKey}", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')")
    public QuartzJobVO modify(@PathVariable String jobKey,
                              @Valid @Validated @RequestBody QuartzJobUpdateFrom from) {
        return managementService.update(jobKey, from);
    }

    /**
     * 删除普通 Quartz Job 及其唯一 Trigger。
     *
     * @param jobKey 要删除的 JobKey；内置 Job 不能删除
     */
    @Audit("'删除 Quartz Job'")
    @DeleteMapping(value = "/jobs/{jobKey}", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')")
    public void delete(@PathVariable String jobKey) {
        managementService.delete(jobKey);
    }

    /**
     * 暂停 Job 的唯一 Trigger。
     *
     * @param jobKey 要暂停的 JobKey
     */
    @Audit("'暂停 Quartz Job'")
    @PostMapping(value = "/jobs/{jobKey}/pause", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')")
    public void pause(@PathVariable String jobKey) {
        managementService.pause(jobKey);
    }

    /**
     * 恢复 Job 的唯一 Trigger。
     *
     * @param jobKey 要恢复的 JobKey
     */
    @Audit("'恢复 Quartz Job'")
    @PostMapping(value = "/jobs/{jobKey}/resume", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')")
    public void resume(@PathVariable String jobKey) {
        managementService.resume(jobKey);
    }

    /**
     * 立即触发一次 Job，不创建临时 Trigger，也不接受参数覆盖。
     *
     * @param jobKey 要立即执行的 JobKey
     */
    @Audit("'立即触发 Quartz Job'")
    @PostMapping(value = "/jobs/{jobKey}/trigger", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void triggerNow(@PathVariable String jobKey) {
        managementService.triggerNow(jobKey);
    }

    /**
     * 查询 Trigger 运行状态和下一次触发时间。
     *
     * @param triggerKey TriggerKey 名称或完整 TriggerKey
     * @return Trigger 安全详情；不存在时由统一异常处理返回 404
     */
    @Audit("'查询 Quartz Trigger'")
    @GetMapping(value = "/triggers/{triggerKey}", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS', 'ROLE_AUDIT')")
    public QuartzTriggerVO triggerDetail(@PathVariable String triggerKey) {
        return managementService.triggerDetail(triggerKey);
    }

    /**
     * 分页查询执行历史。
     *
     * @param page 分页页码和页大小
     * @param from JobKey、TriggerKey、状态和开始时间范围
     * @return 执行历史分页；无匹配时 records 为空且不返回 null
     */
    @Audit("'查询 Quartz 执行历史'")
    @GetMapping(value = "/execution-history", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS', 'ROLE_AUDIT')")
    public IPage<QuartzExecutionHistoryVO> executionHistory(PageFrom page, QuartzHistoryQueryFrom from) {
        return managementService.executionHistory(page, from);
    }

    /**
     * 查询单条执行历史。
     *
     * @param id 执行历史唯一标识
     * @return 执行历史详情；不存在时由统一异常处理返回 404
     */
    @Audit("'查询 Quartz 执行历史详情'")
    @GetMapping(value = "/execution-history/{id}", version = "1.0.0")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS', 'ROLE_AUDIT')")
    public QuartzExecutionHistoryVO executionHistory(@PathVariable UUID id) {
        return managementService.executionHistory(id);
    }
}
