package com.devops00.spectra.upload.service;

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.upload.javabean.entity.FileInfo;

/// 文件信息服务
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 11:35
public interface FileInfoService extends BaseService<FileInfo> {

    /// 根据hash值查询文件是否已经上穿过
    ///
    /// @param hash hash值
    /// @return 文件信息，可能为null
    FileInfo findByHash(String hash);

    /// 增加引用计数
    void incrRefCount(String id);
}
