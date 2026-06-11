package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.upload.javabean.entity.FileType;
import com.devops00.spectra.upload.mapper.FileTypeMapper;
import com.devops00.spectra.upload.service.FileTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 文件类型服务实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/6 15:32
@Slf4j
@Service
public class FileTypeServiceImpl extends BaseServiceImpl<FileTypeMapper, FileType> implements FileTypeService {
}
