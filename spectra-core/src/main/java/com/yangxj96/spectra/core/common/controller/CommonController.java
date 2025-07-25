package com.yangxj96.spectra.core.common.controller;

import com.yangxj96.spectra.core.common.service.CommonService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 通用的一些接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Resource
    private CommonService bindService;


    /**
     * 获取验证码
     */
    @GetMapping("/kaptcha")
    public void kaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        bindService.generateKaptcha(request, response);
    }

    @GetMapping("/cache/{v}")
    public String cache(@PathVariable String v){
        return bindService.cache(v);
    }

}
