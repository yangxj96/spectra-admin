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

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.core.system.javabean.vo.CPUInfoVO;
import com.devops00.spectra.core.system.javabean.vo.JVMInfoVO;
import com.devops00.spectra.core.system.javabean.vo.RAMInfoVO;
import com.devops00.spectra.core.system.service.ServiceMonitorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 服务器信息监控
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@RestController
@RequestMapping("/service/monitor")
public class ServiceMonitorController {

    private final ServiceMonitorService bindService;

    public ServiceMonitorController(ServiceMonitorService bindService) {
        this.bindService = bindService;
    }

    /**
     * 获取服务器 CPU 信息
     *
     * @return CPU 信息
     */
    @GetMapping("/getCPUInfo")
    @PreAuthorize("hasRole(@sec.administrators())")
    public CPUInfoVO getCPUInfo() {
        return bindService.getCPUInfo();
    }

    /**
     * 获取服务器内存信息
     *
     * @return 内存信息
     */
    @GetMapping("/getRAMInfo")
    @PreAuthorize("hasRole(@sec.administrators())")
    public RAMInfoVO getRAMInfo() {
        return bindService.getRAMInfo();
    }

    /**
     * 获取服务器内存信息
     *
     * @return 内存信息
     */
    @GetMapping("/getJVMInfo")
    @PreAuthorize("hasRole(@sec.administrators())")
    public JVMInfoVO getJVMInfo() {
        return bindService.getJVMInfo();
    }

}
