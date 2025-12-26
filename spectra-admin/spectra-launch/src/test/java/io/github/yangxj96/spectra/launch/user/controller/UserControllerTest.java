package io.github.yangxj96.spectra.launch.user.controller;


import com.google.common.collect.Lists;
import io.github.yangxj96.spectra.core.javabean.user.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.service.user.UserService;
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
    void created() {
        // 最小化新建用户
        UserSaveFrom from = new UserSaveFrom();
        from.setStatus((short) 0);
        from.setEmail("yangxj96@gmail.com");
        from.setCountry("China");
        from.setCity("Kunming");
        from.setOrganizationId(1970016645676978177L);
        from.setRoleIds(Lists.newArrayList(1932682189593350146L));

        userService.create(from);

        Assertions.assertTrue(true, "测试完成");
    }


}
