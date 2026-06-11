package com.devops00.spectra.ai.service.impl;


import com.devops00.spectra.ai.javabean.entity.AiSession;
import com.devops00.spectra.ai.mapper.AiSessionMapper;
import com.devops00.spectra.ai.service.AiSessionService;
import com.devops00.spectra.common.base.BaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI模块Session存储Service默认实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/5 17:14
 */
@Slf4j
@Service
public class AiSessionServiceImpl extends BaseServiceImpl<AiSessionMapper, AiSession> implements AiSessionService {
}
