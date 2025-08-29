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
