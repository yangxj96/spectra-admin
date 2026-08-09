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

package com.devops00.spectra.core.auth.controller;

import com.devops00.spectra.core.auth.javabean.from.BindEmailFrom;
import com.devops00.spectra.core.auth.javabean.from.BindPhoneFrom;
import com.devops00.spectra.core.auth.javabean.vo.AccountVO;
import com.devops00.spectra.core.auth.service.AccountService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 账号绑定控制器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
@Slf4j
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取当前用户绑定的账号列表
     */
    @ULog("'获取账号绑定列表'")
    @GetMapping(value = "/list", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ACCOUNT:QUERY')")
    public List<AccountVO> list() {
        UUID userId = SecUtil.getCurrentUserId();
        var accounts = accountService.listByUserId(userId);

        return accounts.stream().map(account -> {
            var vo = new AccountVO();
            vo.setId(account.getId());
            vo.setType(account.getType());
            vo.setLoginName(getLoginName(account));
            vo.setStatus(account.getStatus());
            vo.setVerified(account.getVerified());
            vo.setCurrent(false);
            return vo;
        }).toList();
    }

    /**
     * 绑定手机号
     */
    @ULog("'绑定手机号'")
    @PostMapping(value = "/bindPhone", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ACCOUNT:UPDATE')")
    public void bindPhone(@Validated @RequestBody BindPhoneFrom params) {
        UUID userId = SecUtil.getCurrentUserId();
        accountService.bindPhone(userId, params.getPhone(), params.getCode());
    }

    /**
     * 绑定邮箱
     */
    @ULog("'绑定邮箱'")
    @PostMapping(value = "/bindEmail", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ACCOUNT:UPDATE')")
    public void bindEmail(@Validated @RequestBody BindEmailFrom params) {
        UUID userId = SecUtil.getCurrentUserId();
        accountService.bindEmail(userId, params.getEmail(), params.getCode());
    }

    /**
     * 解绑账号
     */
    @ULog("'解绑账号'")
    @DeleteMapping(value = "/unbind/{accountId}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ACCOUNT:UPDATE')")
    public void unbind(@PathVariable UUID accountId) {
        UUID userId = SecUtil.getCurrentUserId();
        accountService.unbind(userId, accountId);
    }

    /**
     * 根据账号类型获取显示名称
     */
    private String getLoginName(com.devops00.spectra.core.auth.javabean.entity.Account account) {
        return switch (account.getType()) {
            case PASSWORD -> account.getLoginName();
            case SMS -> account.getPhone();
            case EMAIL -> account.getEmail();
            default -> "";
        };
    }
}
