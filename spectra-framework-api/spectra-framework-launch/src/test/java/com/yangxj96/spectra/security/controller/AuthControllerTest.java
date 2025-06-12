package com.yangxj96.spectra.security.controller;

import com.yangxj96.spectra.security.entity.dto.Account;
import com.yangxj96.spectra.security.service.AccountService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


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