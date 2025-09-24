package io.github.yangxj96.spectra.core.auth.configure;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
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
    private OperationLogService logService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        var datum = OperationLog.builder()
                .type(SysLogType.SAFETY)
                .explain("登录")
                .status(Short.parseShort(String.valueOf(response.getStatus())))
                .ip(IpUtils.getClientIP(request))
                .url(request.getRequestURI())
                .method(request.getMethod())
                .createdBy(Long.parseLong(loginId.toString()))
                .updatedBy(Long.parseLong(loginId.toString()))
                .build();
        logService.save(datum);
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        var datum = OperationLog.builder()
                .type(SysLogType.SAFETY)
                .explain("登出")
                .status(Short.parseShort(String.valueOf(response.getStatus())))
                .ip(IpUtils.getClientIP(request))
                .url(request.getRequestURI())
                .method(request.getMethod())
                .createdBy(Long.parseLong(loginId.toString()))
                .updatedBy(Long.parseLong(loginId.toString()))
                .build();
        logService.save(datum);
    }

}
