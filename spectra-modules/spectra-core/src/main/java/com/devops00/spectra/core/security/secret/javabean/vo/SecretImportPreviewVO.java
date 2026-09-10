/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.security.secret.javabean.vo;

import java.util.List;

/** 密钥导入预览视图，只展示编码、版本和冲突信息。 */
public record SecretImportPreviewVO(boolean valid, int entryCount, List<Entry> entries, List<String> conflicts) {

    /** 导入条目元数据。 */
    public record Entry(String code, String category, int version, String fingerprint, boolean registered) {
    }
}
