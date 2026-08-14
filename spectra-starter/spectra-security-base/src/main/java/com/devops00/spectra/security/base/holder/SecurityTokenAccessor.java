/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.holder;

import org.jspecify.annotations.Nullable;

/** 从当前安全请求上下文读取 opaque Access Token 的窄端口。 */
@FunctionalInterface
public interface SecurityTokenAccessor {

    /** 读取当前请求中的 Access Token。 */
    @Nullable
    String getCurrentToken();
}
