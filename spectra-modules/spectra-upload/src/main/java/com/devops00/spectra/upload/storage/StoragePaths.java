/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage;

import java.nio.file.Path;

public final class StoragePaths {

    private StoragePaths() {
    }

    public static Path resolve(Path root, String relative) {
        return root.resolve(resolveRelative(relative)).normalize();
    }

    public static Path resolveRelative(String relative) {
        if (relative == null || relative.isBlank()) {
            throw new IllegalArgumentException("storage key cannot be blank");
        }
        var path = Path.of(relative);
        if (path.isAbsolute() || path.getNameCount() == 0 || path.normalize().startsWith("..")) {
            throw new IllegalArgumentException("storage key escapes its root");
        }
        return path.normalize();
    }
}
