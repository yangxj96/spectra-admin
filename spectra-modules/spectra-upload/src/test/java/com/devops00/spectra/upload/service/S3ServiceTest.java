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

package com.devops00.spectra.upload.service;

import com.devops00.spectra.upload.UploadTestApplication;
import com.devops00.spectra.upload.properties.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/// S3协议测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/31 01:32
@Slf4j
@SpringBootTest(classes = UploadTestApplication.class)
public class S3ServiceTest {

    @Autowired
    private S3Properties s3Properties;

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
    public void testPresigner() {
        String uploadUrl = s3Service.createUploadUrl(s3Properties.getBucket(), "images/v2/a.png");
        log.info("uploadUrl: {}", uploadUrl);
    }
}
