package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.OperationLog;
import com.yangxj96.spectra.core.mapper.OperationLogMapper;
import com.yangxj96.spectra.core.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
