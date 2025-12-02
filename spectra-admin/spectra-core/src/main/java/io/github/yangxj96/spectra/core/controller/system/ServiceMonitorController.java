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

package io.github.yangxj96.spectra.core.controller.system;

import io.github.yangxj96.spectra.core.javabean.system.vo.CPUInfoVO;
import io.github.yangxj96.spectra.core.javabean.system.vo.JVMInfoVO;
import io.github.yangxj96.spectra.core.javabean.system.vo.RAMInfoVO;
import io.github.yangxj96.spectra.core.service.system.ServiceMonitorService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器信息监控
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@RestController
@RequestMapping("/service/monitor")
public class ServiceMonitorController {

    @Resource
    private ServiceMonitorService bindService;

    /**
     * 获取服务器CPU信息
     *
     * @return CPU信息
     */
    @GetMapping("/getCPUInfo")
    public CPUInfoVO getCPUInfo() {
        return bindService.getCPUInfo();
    }

    /**
     * 获取服务器内存信息
     *
     * @return 内存信息
     */
    @GetMapping("/getRAMInfo")
    public RAMInfoVO getRAMInfo() {
        return bindService.getRAMInfo();
    }

    /**
     * 获取服务器内存信息
     *
     * @return 内存信息
     */
    @GetMapping("/getJVMInfo")
    public JVMInfoVO getJVMInfo() {
        return bindService.getJVMInfo();
    }

}
