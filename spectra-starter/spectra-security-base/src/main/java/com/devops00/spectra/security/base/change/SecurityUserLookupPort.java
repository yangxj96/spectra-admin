/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.change;

import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 按 opaque token 查询认证主体的端口。
 */
@NullMarked
public interface SecurityUserLookupPort {

    /** 查询 token 对应的主体；无效或过期 token 返回 {@code null}。 */
    @Nullable SecurityUser findByToken(String token);
}
