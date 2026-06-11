package com.devops00.spectra.oa.contract.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.mapper.ContractMapper;
import com.devops00.spectra.oa.contract.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 合同表主表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 11:53
@Slf4j
@Service
public class ContractServiceImpl extends BaseServiceImpl<ContractMapper, Contract> implements ContractService {
}
