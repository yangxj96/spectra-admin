package com.yangxj96.spectra.core.common.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 通用接口的业务层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
public interface CommonService {

    /**
     * 生成验证码
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    void generateKaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * cache测试
     *
     * @return string
     */
    String cache(String v);

}
