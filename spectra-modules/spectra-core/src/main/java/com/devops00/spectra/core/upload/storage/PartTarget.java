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

import java.time.Instant;
import java.util.Map;

/**
 * 承载分片目标相关的不可变数据。
 *
 * @param method    HTTP 请求方法
 * @param url       HTTP 请求地址
 * @param headers   存储请求需要携带的 HTTP 请求头
 * @param expiresAt 该授权变更令牌的失效时间
 * @param attempt   当前分片上传的尝试序号
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record PartTarget(String method, String url, Map<String, String> headers, Instant expiresAt, int attempt) {

    public PartTarget {
        headers = Map.copyOf(headers);
    }
}
