package io.github.yangxj96.spectra.license.controller;

import cn.dev33.satoken.exception.NotImplException;
import io.github.yangxj96.spectra.license.javabean.from.LicenseCreateFrom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 许可接口
 */
@Slf4j
@RestController
@RequestMapping("/license")
public class LicenseController {

    /**
     * 创建许可
     *
     * @param from 创建入参
     */
    @PostMapping
    public void create(@RequestBody LicenseCreateFrom from) {
        log.atDebug().log("请求参数:{}", from);
        throw new NotImplException("暂未实现");
    }

}
