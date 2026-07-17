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


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 合同主接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:22
@RestController
@RequestMapping("/oa/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService bindService;

    @ULog("'分页查询合同'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public IPage<Contract> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
