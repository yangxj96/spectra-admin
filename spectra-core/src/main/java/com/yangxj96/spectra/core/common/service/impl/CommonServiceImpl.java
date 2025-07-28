package com.yangxj96.spectra.core.common.service.impl;

import com.google.code.kaptcha.Producer;
import com.yangxj96.spectra.common.enums.KaptchaType;
import com.yangxj96.spectra.core.common.service.CommonService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 通用接口的业务层实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "CommonService", keyGenerator = "keyGenerator")
public class CommonServiceImpl implements CommonService {

    @Resource
    private Producer kaptchaProducer;

    @Value("${spectra.kaptcha.type}")
    private KaptchaType kaptchaType;

    @Override
    public void generateKaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = request.getSession().getId();
        log.atDebug().log("SessionId:{}", sessionId);
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 保存验证码信息
        //String uuid = IdUtils.simpleUUID();
        //String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        // 验证码文本
        String capStr;
        // 算数方式下的结果
        String code = null;
        // 生成的图片
        BufferedImage image;

        switch (kaptchaType) {
            case MATH -> {
                String capText = kaptchaProducer.createText();
                capStr = capText.substring(0, capText.lastIndexOf("@"));
                code = capText.substring(capText.lastIndexOf("@") + 1);
                image = kaptchaProducer.createImage(capStr);
            }
            case CHAT -> {
                capStr = code = kaptchaProducer.createText();
                image = kaptchaProducer.createImage(capStr);
            }
            default -> throw new RuntimeException("为获取到验证码生成方式");
        }

        ServletOutputStream out = response.getOutputStream();
        try (out) {
            ImageIO.write(image, "jpg", out);
            out.flush();
        }
    }

    @Override
    @Cacheable
    public String cache(String v) {
        return v;
    }
}
