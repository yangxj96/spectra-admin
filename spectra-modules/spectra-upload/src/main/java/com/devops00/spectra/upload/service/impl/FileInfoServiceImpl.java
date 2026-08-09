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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.upload.javabean.converter.FileInfoConverter;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.javabean.from.FilePageFrom;
import com.devops00.spectra.upload.javabean.vo.FileInfoVO;
import com.devops00.spectra.upload.mapper.FileInfoMapper;
import com.devops00.spectra.upload.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/// 文件信息服务默认实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/2 11:35
@Slf4j
@Service
@RequiredArgsConstructor
public class FileInfoServiceImpl extends BaseServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    private final FileInfoConverter converter;

    @Override
    public FileInfo findByHash(String hash) {
        return lambdaQuery()
                .eq(FileInfo::getHash, hash)
                .one();
    }

    @Override
    @Transactional
    public void incrRefCount(UUID id) {
        FileInfo info = this.getById(id);
        info.setRefCount(info.getRefCount() + 1);
        this.updateById(info);
    }

    @Override
    public IPage<FileInfoVO> page(PageFrom page, FilePageFrom params) {
        var wrapper = new LambdaQueryWrapper<FileInfo>()
                .like(StrUtils.isNotBlank(params.getOriginalName()), FileInfo::getOriginalName, params.getOriginalName())
                .eq(params.getStorageType() != null, FileInfo::getStorageType, params.getStorageType())
                .orderByDesc(FileInfo::getCreatedAt);
        var db = this.page(page.toPage(), wrapper);
        return converter.toVOPage(db);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        this.removeById(id);
    }
}
