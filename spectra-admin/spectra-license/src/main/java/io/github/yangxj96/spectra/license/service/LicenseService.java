package io.github.yangxj96.spectra.license.service;

import io.github.yangxj96.spectra.license.javabean.bean.License;

/**
 * 许可服务
 */
public interface LicenseService {


    /**
     * 生成许可证
     */
    void generateLicense(License license);

    /**
     * 验证许可
     */
    void verifyLicense() throws Exception;

}
