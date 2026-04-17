package com.devops00.spectra.upload.service;


import com.devops00.spectra.upload.javabean.domain.MagicRule;
import com.devops00.spectra.upload.javabean.entity.FileType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// 上传service测试
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 00:53
@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UploadServiceTest {


    @Resource
    private FileTypeService fileTypeService;

    private List<MagicRule> magic(String... hexList) {
        return Arrays.stream(hexList)
                .map(h -> {
                    var r = new MagicRule();
                    r.setBytes(h.toUpperCase());
                    r.setOffset(0);
                    return r;
                })
                .toList();
    }

    /// 初始化文件类型
    @Test
    void initFileType() {
        var types = new ArrayList<FileType>();

        // JPEG
        {
            var t = new FileType();
            t.setName("JPEG");
            t.setExtension(List.of(".jpg", ".jpeg"));
            t.setMime(List.of("image/jpeg"));
            t.setMagicRules(magic(
                    "FFD8FFE0",
                    "FFD8FFE1",
                    "FFD8FFDB"
            ));
            t.setMaxSize(20L * 1024 * 1024); // 20MB
            t.setPreviewable(true);
            t.setAllowedUpload(true);
            t.setDangerous(false);
            t.setRemark("JPEG 图片");
            types.add(t);
        }

        // PNG
        {
            var t = new FileType();
            t.setName("PNG");
            t.setExtension(List.of(".png"));
            t.setMime(List.of("image/png"));
            t.setMagicRules(magic("89504E470D0A1A0A"));
            t.setMaxSize(20L * 1024 * 1024);
            t.setPreviewable(true);
            t.setAllowedUpload(true);
            t.setDangerous(false);
            t.setRemark("PNG 图片");
            types.add(t);
        }

        // GIF
        {
            var t = new FileType();
            t.setName("GIF");
            t.setExtension(List.of(".gif"));
            t.setMime(List.of("image/gif"));
            t.setMagicRules(magic(
                    "474946383761", // GIF87a
                    "474946383961"  // GIF89a
            ));
            t.setMaxSize(10L * 1024 * 1024);
            t.setPreviewable(true);
            t.setAllowedUpload(true);
            t.setDangerous(false);
            t.setRemark("GIF 图片");
            types.add(t);
        }

        // PDF
        {
            var t = new FileType();
            t.setName("PDF");
            t.setExtension(List.of(".pdf"));
            t.setMime(List.of("application/pdf"));
            t.setMagicRules(magic("25504446"));
            t.setMaxSize(100L * 1024 * 1024);
            t.setPreviewable(true);
            t.setAllowedUpload(true);
            t.setDangerous(false);
            t.setRemark("PDF 文档");
            types.add(t);
        }

        // ZIP
        {
            var t = new FileType();
            t.setName("ZIP");
            t.setExtension(List.of(".zip"));
            t.setMime(List.of("application/zip"));
            t.setMagicRules(magic("504B0304"));
            t.setMaxSize(200L * 1024 * 1024);
            t.setPreviewable(false);
            t.setAllowedUpload(true);
            t.setDangerous(false);
            t.setRemark("ZIP 压缩包");
            types.add(t);
        }

        fileTypeService.saveBatch(types);
    }


    /// 尝试读取文件类型
    @Test
    void getFileType() {
        List<FileType> fileTypes = fileTypeService.list();
        for (FileType fileType : fileTypes) {
            log.debug(fileType.toString());
        }
    }


}
