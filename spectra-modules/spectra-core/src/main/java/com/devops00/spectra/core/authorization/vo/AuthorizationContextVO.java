/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.authorization.vo;

import java.util.Set;

/**
 * 当前用户授权上下文。仅返回稳定权限编码，不返回角色名称或旧权限对象。
 */
public record AuthorizationContextVO(Set<String> permissions, Set<String> grantablePermissions) {
}
