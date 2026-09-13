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

package com.devops00.spectra.oa.file.reference;

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 统一创建 OA 文件引用命令，避免业务服务散落字符串协议。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class OaFileReferenceBinder {

    public FileReferenceCommand content(UUID fileAssetId, OaFileReferenceType type, UUID referenceId, String displayName) {
        return new FileReferenceCommand(fileAssetId, type.value(), referenceId, "CONTENT", displayName);
    }
}
