package com.yangxj96.spectra.service.core;

import com.yangxj96.spectra.common.enums.AccountType;
import com.yangxj96.spectra.common.enums.UserState;
import com.yangxj96.spectra.service.auth.javabean.entity.Account;
import com.yangxj96.spectra.service.auth.javabean.entity.User;
import com.yangxj96.spectra.service.auth.service.AccountService;
import com.yangxj96.spectra.service.auth.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

/**
 * 用户认证接口单元测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@SpringBootTest
class AuthControllerTest {

    @Resource
    private BCryptPasswordEncoder encoder;

    @Resource
    private AccountService accountService;

    @Resource
    private UserService userService;

    @Test
    void addSysadmin() {
        var datum = Account.builder()
                .username("sysadmin@pt.com")
                .password(encoder.encode("sysadmin"))
                .type(AccountType.DEFAULT)
                .userId(0L)
                .build();
        boolean save = accountService.save(datum);
        Assert.isTrue(save, "保存失败");
    }

    @Test
    void addUser() {
        var datum = User.builder()
                .name("平台管理员")
                .email("sysadmin@pt.com")
                .avatar(null)
                .state(UserState.NORMAL)
                .build();
        boolean save = userService.save(datum);
        Assert.isTrue(save, "保存失败");
    }

}