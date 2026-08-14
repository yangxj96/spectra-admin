/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.holder;

import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.Nullable;

/** 从 Security Session 读取当前身份源主体的窄端口。 */
public interface SecuritySessionReader {

    /** 读取当前请求对应的主体。 */
    @Nullable
    SecurityUser getCurrentUser();

    /** 按 opaque Access Token 读取当前主体。 */
    @Nullable
    SecurityUser getCurrentUser(String token);
}
