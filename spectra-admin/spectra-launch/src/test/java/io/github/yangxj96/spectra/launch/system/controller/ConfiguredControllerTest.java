package io.github.yangxj96.spectra.launch.system.controller;

import io.github.yangxj96.spectra.core.javabean.system.entity.Configured;
import io.github.yangxj96.spectra.core.service.system.ConfiguredService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统配置项测试
 */
@Slf4j
@SpringBootTest
class ConfiguredControllerTest {

    @Resource
    private ConfiguredService configuredService;

    @Test
    void initData() {
        var list = new ArrayList<Configured>();
        list.add(Configured.builder().key("system.watermark.enable").value("true").remarks("是否开启水").build());
        list.add(Configured.builder().key("system.watermark.type").value("1").remarks("水印类型,1-系统生成 2-固定值").build());
        list.add(Configured.builder().key("system.watermark.fixed.value" ).value("yangxj96.com,2025-11-06").remarks("固定值水印类型的值").build());
        configuredService.saveBatch(list);
    }

    @Test
    void listConfig(){
        List<Configured> configureds = configuredService.list();
        Assertions.assertNotNull(configureds);
    }

}
