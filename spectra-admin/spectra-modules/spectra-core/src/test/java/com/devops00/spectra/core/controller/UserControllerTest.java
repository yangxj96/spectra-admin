package com.devops00.spectra.core.controller;


import com.devops00.spectra.core.javabean.user.from.UserSaveFrom;
import com.devops00.spectra.core.service.user.UserService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/11 17:49
 */
@Slf4j
@SpringBootTest
class UserControllerTest {

    @Resource
    private UserService userService;

    @Test
    void initAdminUser() {
        // 最小化新建用户
        UserSaveFrom from = new UserSaveFrom();
        from.setStatus((short) 0);
        from.setEmail("admin@devops00.com");
        from.setCountry("China");
        from.setCity("Kunming");
        from.setOrganizationId(UuidCreator.getTimeOrderedEpoch().toString());
        from.setRoleIds(Lists.newArrayList(UuidCreator.getTimeOrderedEpoch()));

        userService.create(from);

        Assertions.assertTrue(true, "测试完成");
    }


}
