package io.github.yangxj96.spectra.launch.user.controller;

import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PermissionControllerTest {


    @Resource
    private AuthorityService authorityService;


    @Test
    void addAuthority(){
        Authority dictAuthority = Authority.builder()
                .pid(1L)
                .name("字典管理")
                .code("DICT:*")
                .build();

        authorityService.save(dictAuthority);

        authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("字典新增").code("DICT:INSERT").build());
        authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("字典删除").code("DICT:DELETE").build());
        authorityService.save(Authority.builder().pid(dictAuthority.getId()).name("字典修改").code("DICT:UPDATE").build());
    }



}