/*
 *  Copyright 2018-2025 yangxj96
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
        log.debug("请求参数:{}", from);
        throw new NotImplException("暂未实现");
    }

}
