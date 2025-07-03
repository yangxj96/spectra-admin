package com.yangxj96.spectra.starter.common.configure;

import com.yangxj96.spectra.starter.common.entity.ULogEntity;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 日志消息发送器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/3
 */
public class ULogEventPublisher {

    private final ApplicationEventPublisher publisher;

    public ULogEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void save(ULogEntity entity) {
        publisher.publishEvent(entity);
    }

}
