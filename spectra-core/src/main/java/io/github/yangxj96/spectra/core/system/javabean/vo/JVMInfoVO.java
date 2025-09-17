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

package io.github.yangxj96.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JVM信息
 */
@Data
@Builder
public class JVMInfoVO {

    /**
     * JVM 名称：如 "OpenJDK 64-Bit Server VM"
     */
    private String jvmName;

    /**
     * 供应商：如 "Oracle Corporation", "Eclipse Foundation"
     */
    private String jvmVendor;

    /**
     * 版本：如 "17.0.8+7-LTS", "25.382-b05"
     */
    private String jvmVersion;

    /**
     * JVM 规范名称：通常是 "Java Virtual Machine Specification"
     */
    private String jvmSpecName;

    /**
     * JVM 规范版本：如 "17"
     */
    private String jvmSpecVersion;

    /**
     * JVM 规范供应商
     */
    private String jvmSpecVendor;

    /**
     * Java 版本（同 jvmVersion 略有不同）
     */
    private String javaVersion;

    /**
     * JVM 安装路径
     */
    private String javaHome;

    /**
     * JAVA供应商
     */
    private String javaVendor;

    /**
     * 供应商网址
     */
    private String javaVendorUrl;

    /**
     * 启动时间
     */
    private Date startTime;

    /**
     * "进程ID@主机名"，提取 PID
     */
    private String pid;

    /**
     * 进程ID
     */
    private String processId;

    /**
     * 启动参数
     */
    private List<String> jvmArguments;

    /**
     * 系统属性
     */
    private Map<String,String> systemProperties;

    /**
     * 运行时类路径
     */
    private String classPath;

    /**
     * 本地库路径
     */
    private String libraryPath;

}
