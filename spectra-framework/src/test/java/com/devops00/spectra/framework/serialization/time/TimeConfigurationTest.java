/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework.serialization.time;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/** 系统时间 Bean 配置测试。 */
class TimeConfigurationTest {

    @Test
    void providesUtcClockForPersistenceAndAudit() {
        Clock clock = new TimeConfiguration().systemClock();

        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
