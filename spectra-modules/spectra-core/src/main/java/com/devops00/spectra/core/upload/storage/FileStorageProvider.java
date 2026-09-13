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

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 定义文件存储相关的调用契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface FileStorageProvider {

    /**
     * 处理类型相关数据。
     *
     * @return 存储提供器类型数据。
     */
    StorageProviderType type();

    /**
     * 构建分片上传。
     *
     * @param uploadId   上传标识。
     * @param container  文件存储所在的容器或存储桶。
     * @param key        查询或操作所用的键。
     * @param totalParts 总数部分参数。
     * @return 存储分片上传数据。
     */
    StorageMultipart createMultipart(UUID uploadId, String container, String key, int totalParts);

    /**
     * 构建分片目标。
     *
     * @param multipart  分片上传参数。
     * @param partNumber 分片编号参数。
     * @param partSize   分片大小参数。
     * @param partSha256 分片参数。
     * @param expiresAt  过期参数。
     * @param attempt    尝试参数。
     * @return 分片目标数据。
     */
    PartTarget createPartTarget(StorageMultipart multipart, int partNumber, long partSize, String partSha256,
                                Instant expiresAt, int attempt);

    /**
     * 处理本地分片相关数据。
     *
     * @param multipart      分片上传参数。
     * @param partNumber     分片编号参数。
     * @param content        内容参数。
     * @param expectedSize   大小参数。
     * @param expectedSha256 预期的文件 SHA-256 摘要。
     * @return 分片数据。
     */
    StoredPart putLocalPart(StorageMultipart multipart, int partNumber, InputStream content, long expectedSize,
                            String expectedSha256);

    /**
     * 处理外部分片相关数据。
     *
     * @param multipart      分片上传参数。
     * @param partNumber     分片编号参数。
     * @param expectedSize   大小参数。
     * @param expectedSha256 预期的文件 SHA-256 摘要。
     * @param providerEtag   提供器ETag参数。
     * @return 分片数据。
     */
    StoredPart confirmExternalPart(StorageMultipart multipart, int partNumber, long expectedSize, String expectedSha256,
                                   String providerEtag);

    /**
     * 完成分片上传。
     *
     * @param multipart 分片上传参数。
     * @param parts     部分参数。
     */
    void completeMultipart(StorageMultipart multipart, List<StoredPart> parts);

    /**
     * 取消分片上传。
     *
     * @param multipart 分片上传参数。
     */
    void abortMultipart(StorageMultipart multipart);

    /**
     * 打开文件存储。
     *
     * @param container  文件存储所在的容器或存储桶。
     * @param key        查询或操作所用的键。
     * @param rangeStart 范围起始参数。
     * @param rangeEnd   范围结束参数。
     * @return 存储数据。
     */
    StorageObject open(String container, String key, Long rangeStart, Long rangeEnd);

    /**
     * 删除或清理文件存储。
     *
     * @param container 文件存储所在的容器或存储桶。
     * @param key       查询或操作所用的键。
     */
    void delete(String container, String key);

    /**
     * 处理文件存储相关数据。
     *
     * @param container 文件存储所在的容器或存储桶。
     * @param key       查询或操作所用的键。
     * @return 条件判断结果。
     */
    boolean exists(String container, String key);

    /**
     * 处理健康状态相关数据。
     *
     * @return 存储健康状态数据。
     */
    StorageHealth health();
}
