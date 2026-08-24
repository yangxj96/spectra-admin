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

package com.devops00.spectra.core.system.javabean.vo;

import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Web 端启动阶段所需的公开配置聚合响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemBootstrapVO {

    /** 系统基础信息。 */
    private SystemPublicConfigVO system;

    /** 加解密配置。 */
    private CryptoConfigVO crypto;

    /** 系统初始化状态。 */
    private SystemInitializationStatusVO initialization;
}
