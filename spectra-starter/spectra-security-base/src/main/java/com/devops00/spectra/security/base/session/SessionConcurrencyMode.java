/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.session;

/** 多端会话并发策略。 */
public enum SessionConcurrencyMode {
    /** 允许新会话。 */
    ALLOW,
    /** 新会话建立前撤销旧会话。 */
    KICK_OLD,
    /** 达到上限后拒绝新会话。 */
    REJECT_NEW
}
