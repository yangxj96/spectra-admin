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

package com.devops00.spectra.core.system.javabean.converter;

import java.lang.management.RuntimeMXBean;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.devops00.spectra.core.system.javabean.vo.JVMInfoVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;

/// JVM 信息 MapStruct 转换器。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface JVMInfoConverter {

    /// 运行时 JVM 信息转视图对象。
    @Mapping(target = "jvmName", source = "runtimeMXBean.vmName")
    @Mapping(target = "jvmVendor", source = "runtimeMXBean.vmVendor")
    @Mapping(target = "jvmVersion", source = "runtimeMXBean.vmVersion")
    @Mapping(target = "jvmSpecName", source = "runtimeMXBean.specName")
    @Mapping(target = "jvmSpecVersion", source = "runtimeMXBean.specVersion")
    @Mapping(target = "jvmSpecVendor", source = "runtimeMXBean.specVendor")
    @Mapping(target = "startTime", source = "runtimeMXBean.startTime")
    @Mapping(target = "pid", source = "runtimeMXBean.name")
    @Mapping(target = "processId", expression = "java(runtimeMXBean.getName().split(\"@\")[0])")
    @Mapping(target = "jvmArguments", source = "runtimeMXBean.inputArguments")
    @Mapping(target = "classPath", source = "runtimeMXBean.classPath")
    @Mapping(target = "libraryPath", source = "runtimeMXBean.libraryPath")
    JVMInfoVO toVO(RuntimeMXBean runtimeMXBean, String javaVersion, String javaHome, String javaVendor, String javaVendorUrl,
            Map<String, String> systemProps);
}
