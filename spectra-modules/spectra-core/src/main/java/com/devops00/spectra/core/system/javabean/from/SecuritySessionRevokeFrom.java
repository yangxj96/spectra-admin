/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

import com.devops00.spectra.common.constant.ClientType;

import java.util.UUID;

/** 按用户和客户端撤销 Session/Token 的请求；不接受明文 Token。 */
public record SecuritySessionRevokeFrom(UUID userId, ClientType clientType, String reason, boolean confirmed) {
}
