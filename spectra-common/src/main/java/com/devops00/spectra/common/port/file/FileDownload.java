/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.io.IOException;
import java.io.InputStream;

/** 受授权的文件流结果，不暴露持久化实体和存储 SDK。 */
public record FileDownload(InputStream stream,
                           String displayName,
                           String contentType,
                           long size,
                           Long rangeStart,
                           Long rangeEnd)
        implements
            AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
