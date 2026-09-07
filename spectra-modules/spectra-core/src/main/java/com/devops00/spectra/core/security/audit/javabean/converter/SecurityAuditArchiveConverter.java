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

package com.devops00.spectra.core.security.audit.javabean.converter;

import com.devops00.spectra.core.security.audit.archive.SecurityAuditArchiveOrchestrator;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditArchiveManifestVO;
import com.devops00.spectra.framework.serialization.mapper.GlobalMapperConfig;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.mapstruct.Mapper;

/**
 * 安全审计归档内部视图和 HTTP 响应 VO 的转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface SecurityAuditArchiveConverter {

    /**
     * 将内部归档 manifest 视图转换为 API 响应；时间字段由 {@link TimeMapper} 转换。
     *
     * @param source 内部归档 manifest 视图
     * @return 对外归档 manifest 视图
     */
    SecurityAuditArchiveManifestVO toManifestVO(SecurityAuditArchiveOrchestrator.ManifestView source);
}
