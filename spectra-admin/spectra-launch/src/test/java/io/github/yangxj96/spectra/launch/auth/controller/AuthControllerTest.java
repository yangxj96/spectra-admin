package io.github.yangxj96.spectra.launch.auth.controller;

import io.github.yangxj96.spectra.core.template.IpLocationTemplate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class AuthControllerTest {

    @Resource
    private IpLocationTemplate ipLocationTemplate;

    /**
     * 测试IP转区域效果
     */
    @Test
    void testIpCheck() {
        var region = ipLocationTemplate.getCityEn("106.60.114.81");
        log.atInfo().log("当前地址:{}", region);
    }


}
