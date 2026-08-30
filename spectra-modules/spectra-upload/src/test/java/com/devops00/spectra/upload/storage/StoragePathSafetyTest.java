/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StoragePathSafetyTest {

    @Test
    void rejectsPathTraversalInObjectKeys() {
        assertThrows(IllegalArgumentException.class, () -> StoragePaths.resolveRelative("../outside"));
        assertThrows(IllegalArgumentException.class, () -> StoragePaths.resolveRelative("a/../../outside"));
    }
}
