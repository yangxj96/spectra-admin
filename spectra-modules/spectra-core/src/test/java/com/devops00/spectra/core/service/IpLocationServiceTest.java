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

package com.devops00.spectra.core.service;

import com.devops00.spectra.core.common.service.IpLocationService;
import com.devops00.spectra.core.common.service.impl.IpLocationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * IP转地址测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/26 17:56
 */
@Slf4j
public class IpLocationServiceTest {

    private final IpLocationService ipLocationService = new IpLocationServiceImpl();

    /**
     * 测试IP转区域效果
     */
    @Test
    void testIpCheck() {
        var region = ipLocationService.getCityEn("106.60.114.81", 3);
        log.atInfo().log("当前地址:{}", region);
    }
}
