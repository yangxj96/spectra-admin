/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.holder;

import com.devops00.spectra.common.port.security.UserOnlineVO;

import java.util.List;

/** Security Session 查询窄端口。 */
@FunctionalInterface
public interface SecuritySessionQuery {

    /** 查询当前在线用户及其非敏感会话摘要。 */
    List<UserOnlineVO> listOnlineUsers();
}
