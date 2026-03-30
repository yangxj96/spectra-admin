package com.devops00.spectra.oa.document.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.document.javabean.entity.Document;
import com.devops00.spectra.oa.document.mapper.DocumentMapper;
import com.devops00.spectra.oa.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 文档表主表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 14:13
@Slf4j
@Service
public class DocumentServiceImpl extends BaseServiceImpl<DocumentMapper, Document> implements DocumentService {
}
