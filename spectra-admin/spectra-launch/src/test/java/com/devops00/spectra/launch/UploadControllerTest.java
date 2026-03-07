package com.devops00.spectra.launch;


import com.devops00.spectra.upload.javabean.entity.FileType;
import com.devops00.spectra.upload.service.FileTypeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传相关测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/6 15:44
 */
@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UploadControllerTest {

    @Resource
    private FileTypeService fileTypeService;

    /// 初始化文件类型
    @Test
    void initFileType() {
        var types = new ArrayList<FileType>();

        // JPEG
        {
            var t = new FileType();
            t.setName("JPEG");
            t.setExtension(List.of(new String[]{".jpg", ".jpeg"}));
            t.setMime(List.of(new String[]{"image/jpeg"}));
            t.setMagicNumber(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            t.setMagicOffset(0);
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
            t.setExtension(List.of(new String[]{".png"}));
            t.setMime(List.of(new String[]{"image/png"}));
            t.setMagicNumber(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            t.setMagicOffset(0);
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
            t.setExtension(List.of(new String[]{".gif"}));
            t.setMime(List.of(new String[]{"image/gif"}));
            t.setMagicNumber(new byte[]{0x47, 0x49, 0x46, 0x38});
            t.setMagicOffset(0);
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
            t.setExtension(List.of(new String[]{".pdf"}));
            t.setMime(List.of(new String[]{"application/pdf"}));
            t.setMagicNumber(new byte[]{0x25, 0x50, 0x44, 0x46});
            t.setMagicOffset(0);
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
            t.setExtension(List.of(new String[]{".zip"}));
            t.setMime(List.of(new String[]{"application/zip"}));
            t.setMagicNumber(new byte[]{0x50, 0x4B, 0x03, 0x04});
            t.setMagicOffset(0);
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
