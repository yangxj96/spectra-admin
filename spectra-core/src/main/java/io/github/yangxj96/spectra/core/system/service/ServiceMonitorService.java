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

package io.github.yangxj96.spectra.core.system.service;

import io.github.yangxj96.spectra.core.system.javabean.vo.CPUInfoVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.JVMInfoVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.RAMInfoVO;

/**
 * 服务器信息监控
 */
public interface ServiceMonitorService {

    /**
     * 获取服务器CPU信息
     *
     * @return 服务器CPU信息
     */
    CPUInfoVO getCPUInfo();


    /**
     * 获取服务器内存信息
     *
     * @return 服务器内存信息
     */
    RAMInfoVO getRAMInfo();

    /**
     * 获取JVM信息
     *
     * @return JVM信息
     */
    JVMInfoVO getJVMInfo();
}
