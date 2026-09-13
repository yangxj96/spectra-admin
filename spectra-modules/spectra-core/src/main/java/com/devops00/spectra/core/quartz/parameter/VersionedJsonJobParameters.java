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

package com.devops00.spectra.core.quartz.parameter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 已完成 schema 校验、可安全写入 JobDataMap 的版本化参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public final class VersionedJsonJobParameters {

    private final String version;
    private final Map<String, Object> values;
    private final String json;

    /** 创建不可变参数快照。 */
    public VersionedJsonJobParameters(String version, Map<String, Object> values, String json) {
        if (version == null || version.isBlank() || json == null || json.isBlank()) {
            throw new IllegalArgumentException("版本化 Job 参数不完整");
        }
        this.version = version;
        this.values = Map.copyOf(new LinkedHashMap<>(values == null ? Map.of() : values));
        this.json = json;
    }

    /**
     * 返回参数数据采用的版本号。
     */
    public String version() {
        return version;
    }

    /**
     * 返回版本化参数中的业务值。
     */
    public Map<String, Object> values() {
        return values;
    }

    /**
     * 将版本化参数编码为 JSON 文本。
     */
    public String json() {
        return json;
    }
}
