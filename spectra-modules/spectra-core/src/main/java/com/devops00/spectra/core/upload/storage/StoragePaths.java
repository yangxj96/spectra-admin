/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.upload.storage;

import java.nio.file.Path;

/**
 * 封装存储路径相关的数据和处理逻辑。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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
