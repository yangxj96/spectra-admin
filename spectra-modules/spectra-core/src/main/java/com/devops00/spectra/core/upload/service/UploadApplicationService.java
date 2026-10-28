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

package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.core.upload.javabean.from.ConfirmPartRequest;
import com.devops00.spectra.core.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.core.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;

import java.io.InputStream;
import java.util.UUID;

/**
 * 文件上传应用层用例契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
public interface UploadApplicationService {

    UploadSessionVO create(CreateUploadRequest request);

    UploadSessionVO status(UUID uploadId);

    PartTargetVO target(UUID uploadId, int partNumber, PartTargetRequest request);

    void putPart(UUID uploadId, int partNumber, InputStream body, long contentLength);

    void confirm(UUID uploadId, int partNumber, ConfirmPartRequest request);

    UploadSessionVO complete(UUID uploadId);

    void cancel(UUID uploadId);
}
