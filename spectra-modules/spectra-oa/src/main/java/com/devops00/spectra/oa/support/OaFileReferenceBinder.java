/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.oa.support;

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** 统一创建 OA 文件引用命令，避免业务服务散落字符串协议。 */
@Component
public class OaFileReferenceBinder {

    public FileReferenceCommand content(UUID fileAssetId, OaFileReferenceType type, UUID referenceId, String displayName) {
        return new FileReferenceCommand(fileAssetId, type.value(), referenceId, "CONTENT", displayName);
    }
}
