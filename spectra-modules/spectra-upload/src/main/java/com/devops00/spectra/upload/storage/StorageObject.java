/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage;

import java.io.IOException;
import java.io.InputStream;

public record StorageObject(InputStream stream, StorageObjectMetadata metadata) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
