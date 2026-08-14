/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.holder;

/** 登录失败计数与临时锁定窄端口。 */
public interface SecurityLoginFailureTracker {

    /** 记录一次登录失败。 */
    void recordLoginFail(String username);

    /** 判断登录失败锁定状态。 */
    boolean isLockedOut(String username);

    /** 清理登录失败计数。 */
    void clearLoginFail(String username);
}
