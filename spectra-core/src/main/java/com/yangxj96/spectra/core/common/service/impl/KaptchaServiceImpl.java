package com.yangxj96.spectra.core.common.service.impl;

import com.google.code.kaptcha.Producer;
import com.yangxj96.spectra.common.constant.RedisKey;
import com.yangxj96.spectra.common.exception.KaptchaExpiresException;
import com.yangxj96.spectra.common.properties.KaptchaProperties;
import com.yangxj96.spectra.core.common.service.KaptchaService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务默认实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Service
public class KaptchaServiceImpl implements KaptchaService {

    @Resource
    private Producer kaptchaProducer;

    @Resource
    private KaptchaProperties properties;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Override
    public void generate() throws IOException {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 验证码文本
        String capStr;
        // 算数方式下的结果
        String code;
        // 生成的图片
        BufferedImage image;

        switch (properties.getType()) {
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

        // 存储到缓存中
        redisTemplate.opsForValue().set(RedisKey.KAPTCHA + request.getSession().getId(), code, 5, TimeUnit.MINUTES);

        ServletOutputStream out = response.getOutputStream();
        try (out) {
            ImageIO.write(image, "jpg", out);
            out.flush();
        }
    }

    @Override
    public Boolean isCheck() {
        return properties.getVerify() == Boolean.TRUE;
    }

    @Override
    public String getKaptchaCode() {
        var key = RedisKey.KAPTCHA + request.getSession().getId();
        var val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            throw new KaptchaExpiresException("验证码过期");
        }
        // 这里逻辑上确实是有可能为null的
        return val.toString();
    }

    @Override
    public void deleteBySessionId() {
        var key = RedisKey.KAPTCHA + request.getSession().getId();
        redisTemplate.delete(key);
    }
}
