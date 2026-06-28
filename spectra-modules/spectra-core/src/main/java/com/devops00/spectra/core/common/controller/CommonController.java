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

package com.devops00.spectra.core.common.controller;

import com.devops00.spectra.core.common.service.KaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/// 通用的一些接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/7/25 00:00
@RestController
@RequestMapping("/common")
public class CommonController {

    private final KaptchaService kaptchaService;

    public CommonController(KaptchaService kaptchaService) {
        this.kaptchaService = kaptchaService;
    }

    /// 获取验证码
    @GetMapping(value = "/kaptcha", version = "1.0.0+")
    public void kaptcha() throws IOException {
        kaptchaService.generate();
    }
}
