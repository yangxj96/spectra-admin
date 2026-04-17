package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.mapper.FileInfoMapper;
import com.devops00.spectra.upload.service.FileInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/// 文件信息服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 11:35
@Slf4j
@Service
public class FileInfoServiceImpl extends BaseServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

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


}
