package io.github.yangxj96.spectra.launch.user.controller;

import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import jakarta.annotation.Resource;
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
                .name("角色管理")
                .code("USER:*")
                .build();

        authorityService.save(dictAuthority);
        authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("用户新增").code("USER:INSERT").build());
        authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("用户删除").code("USER:DELETE").build());
        authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("用户修改").code("USER:UPDATE").build());
    }


}