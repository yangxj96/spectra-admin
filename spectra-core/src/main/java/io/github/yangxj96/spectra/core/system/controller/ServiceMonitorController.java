package io.github.yangxj96.spectra.core.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yangxj96.spectra.core.system.javabean.vo.CPUInfoVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.JVMInfoVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.RAMInfoVO;
import io.github.yangxj96.spectra.core.system.service.ServiceMonitorService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器信息监控
 */
@SaCheckLogin
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
