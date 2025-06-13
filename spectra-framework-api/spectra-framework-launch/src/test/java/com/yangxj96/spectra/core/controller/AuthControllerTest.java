package com.yangxj96.spectra.core.controller;

import com.yangxj96.spectra.core.javabean.entity.Account;
import com.yangxj96.spectra.core.service.AccountService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 用户认证接口测试
 *
 * @author Jack Young
 * @since 2025/6/3 23:44
 */
@SpringBootTest
class AuthControllerTest {

    @Resource
    private BCryptPasswordEncoder encoder;

    @Resource
    private AccountService accountService;


    @Test
    void addSysadmin() {
        accountService.save(Account.builder()
                .username("sysadmin")
                .password(encoder.encode("sysadmin"))
                .enable(true)
                .build());
    }

}