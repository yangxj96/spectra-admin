package com.devops00.spectra.oa.document.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.configure.ulog.annotation.ULog;
import com.devops00.spectra.oa.document.javabean.entity.Document;
import com.devops00.spectra.oa.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 文档管理主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:22
@RestController
@RequestMapping("/oa/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService bindService;

    @ULog("分页查询文档")
    @GetMapping("/page")
    public IPage<Document> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
