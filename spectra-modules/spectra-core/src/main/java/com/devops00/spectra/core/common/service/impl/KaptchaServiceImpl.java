/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.common.service.impl;

import com.devops00.spectra.common.exception.KaptchaExpiresException;
import com.devops00.spectra.common.exception.ReadPropertiesException;
import com.devops00.spectra.core.common.service.KaptchaService;
import com.devops00.spectra.framework.configure.kaptcha.properties.KaptchaProperties;
import com.devops00.spectra.common.constant.RedisCacheKey;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码服务默认实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/28 00:00
 */
@Slf4j
@Service
public class KaptchaServiceImpl implements KaptchaService {

    private final Producer kaptchaProducer;

    private final KaptchaProperties properties;

    private final RedisTemplate<String, Object> redisTemplate;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    public KaptchaServiceImpl(Producer kaptchaProducer, KaptchaProperties properties, RedisTemplate<String, Object> redisTemplate,
            HttpServletRequest request, HttpServletResponse response) {
        this.kaptchaProducer = kaptchaProducer;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.request = request;
        this.response = response;
    }

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
            default -> throw new ReadPropertiesException("未获取到验证码生成方式");
        }

        // 存储到缓存中
        redisTemplate.opsForValue().set(RedisCacheKey.KAPTCHA + request.getSession().getId(), code, properties.getDuration());

        var out = response.getOutputStream();
        try (out) {
            ImageIO.write(image, "jpg", out);
            out.flush();
        }
    }

    @Override
    public Boolean isCheck() {
        return properties.getVerify();
    }

    @Override
    public String getKaptchaCode() {
        var key = RedisCacheKey.KAPTCHA + request.getSession().getId();
        var val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            throw new KaptchaExpiresException("验证码过期");
        }
        // 这里逻辑上确实是有可能为null的
        return val.toString();
    }

    @Override
    public void deleteBySessionId() {
        var key = RedisCacheKey.KAPTCHA + request.getSession().getId();
        redisTemplate.delete(key);
    }
}
