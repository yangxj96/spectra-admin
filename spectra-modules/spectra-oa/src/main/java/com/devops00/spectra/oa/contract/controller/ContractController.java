package com.devops00.spectra.oa.contract.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 合同主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:22
@RestController
@RequestMapping("/oa/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService bindService;

    @ULog("分页查询合同")
    @GetMapping("/page")
    public IPage<Contract> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
