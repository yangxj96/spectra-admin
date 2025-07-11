package com.yangxj96.spectra.service.launch.auth;

import com.yangxj96.spectra.common.enums.AccountType;
import com.yangxj96.spectra.service.auth.javabean.entity.Account;
import com.yangxj96.spectra.service.auth.javabean.entity.Authority;
import com.yangxj96.spectra.service.auth.javabean.entity.User;
import com.yangxj96.spectra.service.auth.service.AccountService;
import com.yangxj96.spectra.service.auth.service.AuthorityService;
import com.yangxj96.spectra.service.auth.service.UserService;
import com.yangxj96.spectra.service.launch.SpectraApplication;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户认证接口单元测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@SpringBootTest(classes = SpectraApplication.class)
class AuthControllerTest {

    @Resource
    private BCryptPasswordEncoder encoder;

    @Resource
    private AccountService accountService;

    @Resource
    private UserService userService;

    @Resource
    private AuthorityService authorityService;

    @DynamicPropertySource
    static void jasyptProperties(DynamicPropertyRegistry registry) {
        Dotenv dotenv = Dotenv.configure().directory("../.env").load();
        String jasyptPassword = dotenv.get("JASYPT_ENCRYPTOR_PASSWORD");
        registry.add("jasypt.encryptor.password", () -> jasyptPassword);
    }

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
                .state((short) 0)
                .build();
        boolean save = userService.save(datum);
        Assert.isTrue(save, "保存失败");
    }

    @Test
    void addAuthority() {
        Authority authority = Authority.builder().name("字典权限").code("MENU:*").build();
        var flag = authorityService.save(authority);
        Assertions.assertTrue(flag, "新增权限失败");
        List<Authority> list = new ArrayList<>();
        list.add(Authority.builder().pid(authority.getId()).name("字典新增").code("MENU:INSERT").build());
        list.add(Authority.builder().pid(authority.getId()).name("字典修改").code("MENU:UPDATE").build());
        list.add(Authority.builder().pid(authority.getId()).name("字典删除").code("MENU:DELETE").build());
        flag = authorityService.saveBatch(list);
        Assertions.assertTrue(flag, "新增权限失败");
    }


}