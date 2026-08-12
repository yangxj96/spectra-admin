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

package com.devops00.spectra.oa.leave.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.leave.javabean.from.LeaveCreateFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeavePageFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeaveSubmitFrom;
import com.devops00.spectra.oa.leave.javabean.vo.LeaveVO;
import com.devops00.spectra.oa.leave.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * OA 请假申请接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Slf4j
@RestController
@RequestMapping("/oa/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * 创建请假申请。
     */
    @ULog("'创建请假申请'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:INSERT')")
    public UUID create(@Validated @RequestBody LeaveCreateFrom from) {
        return leaveService.create(from);
    }

    /**
     * 修改请假申请。
     */
    @ULog("'修改请假申请'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:UPDATE')")
    public void update(@PathVariable UUID id, @Validated @RequestBody LeaveCreateFrom from) {
        leaveService.update(id, from);
    }

    /**
     * 分页查询请假申请。
     */
    @ULog("'分页查询请假申请'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:QUERY')")
    public IPage<LeaveVO> page(PageFrom page, LeavePageFrom params) {
        return leaveService.page(page, params);
    }

    /**
     * 查询请假申请详情。
     */
    @ULog("'查询请假申请详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:QUERY')")
    public LeaveVO get(@PathVariable UUID id) {
        return leaveService.get(id);
    }

    /**
     * 提交请假申请。
     */
    @ULog("'提交请假申请'")
    @PostMapping(value = "/{id}/submit", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:UPDATE')")
    public void submit(@PathVariable UUID id, @RequestBody(required = false) LeaveSubmitFrom from) {
        leaveService.submit(id, from);
    }

    /**
     * 撤回请假申请。
     */
    @ULog("'撤回请假申请'")
    @PostMapping(value = "/{id}/withdraw", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:UPDATE')")
    public void withdraw(@PathVariable UUID id) {
        leaveService.withdraw(id);
    }

    /**
     * 取消请假申请。
     */
    @ULog("'取消请假申请'")
    @PostMapping(value = "/{id}/cancel", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_LEAVE:UPDATE')")
    public void cancel(@PathVariable UUID id) {
        leaveService.cancel(id);
    }
}
