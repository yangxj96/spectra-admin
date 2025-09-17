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

package io.github.yangxj96.spectra.core.common.service;

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

    /**
     * 根据SessionID删除验证码
     */
    void deleteBySessionId();

}
