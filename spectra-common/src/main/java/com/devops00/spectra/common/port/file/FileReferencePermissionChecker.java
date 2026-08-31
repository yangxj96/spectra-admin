/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/** 业务模块提供的文件引用读取权限检查端口。 */
public interface FileReferencePermissionChecker {

    boolean supports(String referenceType);

    boolean canRead(String referenceType, UUID referenceId, UUID userId);
}
