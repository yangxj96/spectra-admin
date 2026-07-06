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

package com.devops00.spectra.ocr.controller;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.ocr.model.OcrResult;
import com.devops00.spectra.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/// OCR识别接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@RestController
@RequestMapping("/ocr")
public class OcrController {

    private static final Logger log = LoggerFactory.getLogger(OcrController.class);

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    /// 识别图片中的文字并返回队伍结构
    ///
    /// @param file 上传的图片文件
    /// @return OCR识别结果
    @PostMapping(value = "/recognize", version = "1.0.0+")
    public OcrResult recognize(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("{}收到OCR识别请求, 文件={}, 大小={}B", LogPrefix.OCR.p(),
                file.getOriginalFilename(), file.getSize());
        return ocrService.recognize(file.getInputStream());
    }
}
