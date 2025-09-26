package io.github.yangxj96.spectra.license.runner;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.yangxj96.spectra.license.javabean.bean.License;
import io.github.yangxj96.spectra.license.service.LicenseService;
import io.github.yangxj96.spectra.license.utils.HardwareIdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 许可验证
 */
@Slf4j
@Component
public class LicenseValidationRunner implements ApplicationRunner {

    @Resource
    private LicenseService licenseService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //licenseService.verifyLicense();
        licenseService.generateLicense(License.builder()
                .id(IdWorker.getId())
                .productName("SpectraOpenSource")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(365L * 24 * 3600))
                .hwid(HardwareIdUtil.generateHWID())
                .build());
    }
}
