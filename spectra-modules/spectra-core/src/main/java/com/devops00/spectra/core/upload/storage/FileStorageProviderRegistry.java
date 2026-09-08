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

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件存储 Provider 的启动期索引。
 *
 * <p>Registry 在 Spring 创建时固定 Provider 类型到实现的映射。后续业务只通过本类解析 Provider，
 * 从而保证类型缺失和重复注册使用统一的失败语义。</p>
 */
@Component
public final class FileStorageProviderRegistry {

    private final Map<StorageProviderType, FileStorageProvider> providers;

    /**
     * 创建文件存储 Provider 索引，并校验每个实现声明的类型唯一且非空。
     *
     * @param providerBeans Spring 容器发现的全部文件存储 Provider；可以为空，表示当前没有可用存储实现
     * @throws IllegalStateException 当 Provider bean 为空、未声明类型或多个 Provider 声明同一类型时抛出
     */
    public FileStorageProviderRegistry(List<FileStorageProvider> providerBeans) {
        EnumMap<StorageProviderType, FileStorageProvider> index = new EnumMap<>(StorageProviderType.class);
        for (FileStorageProvider provider : providerBeans) {
            if (provider == null) {
                throw new IllegalStateException("文件存储 Provider 必须声明非空类型");
            }
            StorageProviderType type = provider.type();
            if (type == null) {
                throw new IllegalStateException("文件存储 Provider 必须声明非空类型");
            }
            if (index.containsKey(type)) {
                throw new IllegalStateException("重复注册文件存储 Provider: " + type);
            }
            index.put(type, provider);
        }
        this.providers = Map.copyOf(index);
    }

    /**
     * 获取指定类型的文件存储 Provider，缺失时以文件存储不可用错误终止当前流程。
     *
     * @param type 业务流程选择的存储类型；为空时视为未配置有效存储类型
     * @return 已注册且与类型匹配的 Provider，不会返回 {@code null}
     * @throws FileUploadException 当类型为空或没有对应的已注册 Provider 时抛出，错误码为
     *                             {@link FileErrorCode#FILE_STORAGE_UNAVAILABLE}
     */
    public FileStorageProvider require(StorageProviderType type) {
        return find(type).orElseThrow(() -> unavailable(type));
    }

    /**
     * 查询指定类型的文件存储 Provider，不会因配置缺失抛出领域异常。
     *
     * @param type 要查询的存储类型；为空时不进行查询
     * @return 包含匹配 Provider 的 Optional；类型为空或未注册时返回空 Optional
     */
    public Optional<FileStorageProvider> find(StorageProviderType type) {
        return type == null ? Optional.empty() : Optional.ofNullable(providers.get(type));
    }

    private FileUploadException unavailable(StorageProviderType type) {
        return new FileUploadException(FileErrorCode.FILE_STORAGE_UNAVAILABLE,
                "storage provider is unavailable: " + type);
    }
}
