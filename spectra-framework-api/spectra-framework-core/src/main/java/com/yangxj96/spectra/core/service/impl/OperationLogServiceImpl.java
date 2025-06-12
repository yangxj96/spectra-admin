package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.entity.dto.OperationLog;
import com.yangxj96.spectra.core.mapper.OperationLogMapper;
import com.yangxj96.spectra.core.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
