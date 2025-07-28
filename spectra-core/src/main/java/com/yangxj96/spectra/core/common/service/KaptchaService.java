package com.yangxj96.spectra.core.common.service;

import java.io.IOException;

/**
 * 验证码服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
public interface KaptchaService {

    /**
     * 生成一个验证码
     */
    void generate() throws IOException;

    /**
     * 是否需要进行验证码验证
     *
     * @return 是否需要进行验证
     */
    Boolean isCheck();

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    String getKaptchaCode();
}
