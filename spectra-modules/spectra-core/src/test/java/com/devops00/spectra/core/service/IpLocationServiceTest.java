package com.devops00.spectra.core.service;


import com.devops00.spectra.core.common.service.IpLocationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/// IP转地址测试
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/26 17:56
@Slf4j
@SpringBootTest
public class IpLocationServiceTest {

    @Resource
    private IpLocationService ipLocationService;

    /**
     * 测试IP转区域效果
     */
    @Test
    void testIpCheck() {
        var region = ipLocationService.getCityEn("106.60.114.81", 3);
        log.atInfo().log("当前地址:{}", region);
    }

}
