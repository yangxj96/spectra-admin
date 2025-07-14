package com.yangxj96.spectra.service.launch.core;

import com.yangxj96.spectra.service.core.javabean.entity.Organization;
import com.yangxj96.spectra.service.core.service.OrganizationService;
import com.yangxj96.spectra.service.launch.SpectraApplication;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 组织机构测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@SpringBootTest(classes = SpectraApplication.class)
class OrganizationControllerTest {

    @Resource
    private OrganizationService organizationService;

    @DynamicPropertySource
    static void jasyptProperties(DynamicPropertyRegistry registry) {
        Dotenv dotenv = Dotenv.configure().directory("../.env").load();
        String jasyptPassword = dotenv.get("JASYPT_ENCRYPTOR_PASSWORD");
        registry.add("jasypt.encryptor.password", () -> jasyptPassword);
    }

    /**
     * 添加根组织机构
     */
    @Test
    void addRootOrganization() {
        Organization root = Organization
                .builder()
                .name("道一科技")
                .code("ORG_ROOT")
                .type((short) 1)
                .address("中国")
                .build();
        var flag = organizationService.save(root);
        Assertions.assertTrue(flag, "新增失败");
    }

}
