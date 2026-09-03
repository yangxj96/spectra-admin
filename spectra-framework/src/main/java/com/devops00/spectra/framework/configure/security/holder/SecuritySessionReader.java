/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.holder;

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import org.jspecify.annotations.Nullable;

/** 从 Security Session 读取当前身份源主体的窄端口。 */
public interface SecuritySessionReader {

    /** 读取当前请求对应的主体。 */
    @Nullable
    SecurityPrincipal getCurrentUser();

    /** 按 opaque Access Token 读取当前主体。 */
    @Nullable
    SecurityPrincipal getCurrentUser(String token);
}
