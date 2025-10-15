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

package io.github.yangxj96.spectra.core.auth.configure;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.spectra.common.enums.SysLogType;
import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.core.system.javabean.entity.OperationLog;
import io.github.yangxj96.spectra.core.system.service.OperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * sa-token事件监听器
 */
@Slf4j
@Component
public class SaTokenListenerForApplication extends SaTokenListenerForSimple {

    @Resource
    private ObjectMapper om;

    @Resource
    private OperationLogService logService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        try {
            var datum = OperationLog.builder()
                    .logType(SysLogType.SAFETY)
                    .explain("登录")
                    .status(Short.parseShort(String.valueOf(response.getStatus())))
                    .ip(IpUtils.getClientIP(request))
                    .url(request.getRequestURI())
                    .method(request.getMethod())
                    .args(om.writeValueAsString(loginParameter))
                    .result(tokenValue)
                    .createdBy(Long.parseLong(loginId.toString()))
                    .updatedBy(Long.parseLong(loginId.toString()))
                    .build();
            logService.save(datum);
        } catch (JsonProcessingException e) {
            log.error("序列化登录参数失败,{}", e.getMessage(), e);
        }
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        var datum = OperationLog.builder()
                .logType(SysLogType.SAFETY)
                .explain("登出")
                .status(Short.parseShort(String.valueOf(response.getStatus())))
                .ip(IpUtils.getClientIP(request))
                .url(request.getRequestURI())
                .method(request.getMethod())
                .args(tokenValue)
                .createdBy(Long.parseLong(loginId.toString()))
                .updatedBy(Long.parseLong(loginId.toString()))
                .build();
        logService.save(datum);
    }

}
