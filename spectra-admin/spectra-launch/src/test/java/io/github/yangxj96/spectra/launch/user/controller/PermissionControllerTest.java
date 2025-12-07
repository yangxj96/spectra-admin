package io.github.yangxj96.spectra.launch.user.controller;

import io.github.yangxj96.spectra.core.javabean.user.entity.Authority;
import io.github.yangxj96.spectra.core.service.user.AuthorityService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PermissionControllerTest {

    @Resource
    private AuthorityService authorityService;

    @Test
    void addAuthority() {
        Authority dictAuthority = Authority.builder()
                .pid(1L)
                .name("服务监控")
                .code("USER:*")
                .build();
        boolean flag;
        flag = authorityService.save(dictAuthority);
        Assertions.assertTrue(flag, "保存失败");
        flag = authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("用户新增").code("USER:INSERT").build());
        Assertions.assertTrue(flag, "保存失败");
        flag = authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("用户删除").code("USER:DELETE").build());
        Assertions.assertTrue(flag, "保存失败");
        flag = authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("用户修改").code("USER:UPDATE").build());
        Assertions.assertTrue(flag, "保存失败");
    }


}