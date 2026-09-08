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

package com.devops00.spectra.core.common.service;

import java.io.IOException;

/**
 * 验证码服务
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/28 00:00
 */
public interface KaptchaService {

    /**
     * 生成验证码
     *
     * @throws IOException 读取或创建外部资源失败时抛出。
     */
    void generate() throws IOException;

    /**
     * 是否检查
     *
     * @return 返回验证码校验开关配置；配置项未设置时可能返回 null，调用方应按 Boolean.TRUE 判断是否开启。
     */
    Boolean isCheck();

    /**
     * 校验并一次性消费当前会话验证码。
     *
     * @param code 用户提交的验证码
     * @return 返回验证码是否被成功校验并消费；验证码不存在、过期或已消费时返回 false，Redis 操作失败时抛出异常。
     */
    boolean consumeKaptchaCode(String code);

}
