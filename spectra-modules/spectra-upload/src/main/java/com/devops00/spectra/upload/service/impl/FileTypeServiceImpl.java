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

package com.devops00.spectra.upload.service.impl;

import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.upload.javabean.entity.FileType;
import com.devops00.spectra.upload.mapper.FileTypeMapper;
import com.devops00.spectra.upload.service.FileTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// 文件类型服务实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/6 15:32
@Slf4j
@Service
public class FileTypeServiceImpl extends BaseServiceImpl<FileTypeMapper, FileType> implements FileTypeService {

    @Override
    @Cacheable(cacheNames = "upload:fileType", key = "'allowed-ext'")
    public Set<String> findAllowedExtensions() {
        var types = lambdaQuery()
                .eq(FileType::getAllowedUpload, true)
                .list();
        var extensions = new HashSet<String>();
        for (FileType type : types) {
            if (type.getExtension() != null) {
                for (String ext : type.getExtension()) {
                    extensions.add(ext.toLowerCase());
                }
            }
        }
        return extensions;
    }

    @Override
    @Cacheable(cacheNames = "upload:fileType", key = "'dangerous-ext'")
    public Set<String> findDangerousExtensions() {
        var types = lambdaQuery()
                .eq(FileType::getDangerous, true)
                .list();
        var extensions = new HashSet<String>();
        for (FileType type : types) {
            if (type.getExtension() != null) {
                for (String ext : type.getExtension()) {
                    extensions.add(ext.toLowerCase());
                }
            }
        }
        return extensions;
    }

    @Override
    @Cacheable(cacheNames = "upload:fileType", key = "'allowed-mime'")
    public Set<String> findAllowedMimes() {
        var types = lambdaQuery()
                .eq(FileType::getAllowedUpload, true)
                .isNotNull(FileType::getMime)
                .list();
        var mimes = new HashSet<String>();
        for (FileType type : types) {
            if (type.getMime() != null) {
                for (String mime : type.getMime()) {
                    mimes.add(mime.toLowerCase());
                }
            }
        }
        return mimes;
    }

    @Override
    @Cacheable(cacheNames = "upload:fileType", key = "'dangerous-mime'")
    public Set<String> findDangerousMimes() {
        var types = lambdaQuery()
                .eq(FileType::getDangerous, true)
                .isNotNull(FileType::getMime)
                .list();
        var mimes = new HashSet<String>();
        for (FileType type : types) {
            if (type.getMime() != null) {
                for (String mime : type.getMime()) {
                    mimes.add(mime.toLowerCase());
                }
            }
        }
        return mimes;
    }

    @Override
    @Cacheable(cacheNames = "upload:fileType", key = "'dangerous-magic'")
    public List<FileType> findDangerousWithMagicRules() {
        return lambdaQuery()
                .eq(FileType::getDangerous, true)
                .isNotNull(FileType::getMagicRules)
                .list();
    }
}
