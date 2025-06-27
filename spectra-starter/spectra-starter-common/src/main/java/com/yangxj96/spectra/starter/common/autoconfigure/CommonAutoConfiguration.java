package com.yangxj96.spectra.starter.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangxj96.spectra.starter.common.aspectj.ULogAspectj;
import com.yangxj96.spectra.starter.common.service.ULogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 通用的一些自动配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnClass({ULogService.class, ObjectMapper.class})
    public ULogAspectj uLogAspectj(ULogService service, ObjectMapper om) {
        return new ULogAspectj(service, om);
    }

}
