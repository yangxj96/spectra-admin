package com.yangxj96.spectra.core.auth.service.impl;

import com.yangxj96.spectra.core.auth.service.PermissionService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

/**
 * 权限类,主要用作在SpEL表达式中进行计算
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Service("ss")
public class PermissionServiceImpl implements PermissionService {

    @Override
    public void check() {
        throw new NotImplementedException("权限未实现");
    }
}
