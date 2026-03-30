package com.devops00.spectra.upload.service;


import com.devops00.spectra.upload.UploadTestApplication;
import com.devops00.spectra.upload.properties.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * S3协议测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/31 01:32
 */
@Slf4j
@SpringBootTest(classes = UploadTestApplication.class)
public class S3ServiceTest {

    @Autowired
    private S3Properties  s3Properties;

    @Autowired
    private S3Service s3Service;

    @Test
    public void testList() {
        List<String> files = s3Service.listAllObjects(s3Properties.getBucket());
        for (String file : files) {
            log.info("file: {}", file);
        }
    }

    @Test
    public void testPresigner(){
        String uploadUrl = s3Service.createUploadUrl(s3Properties.getBucket(), "images/v2/a.png");
        log.info("uploadUrl: {}", uploadUrl);
    }
}
