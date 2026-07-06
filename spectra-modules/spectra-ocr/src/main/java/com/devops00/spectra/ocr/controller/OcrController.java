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
import com.devops00.spectra.ocr.model.OcrForm;
import com.devops00.spectra.ocr.model.OcrResult;
import com.devops00.spectra.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    /// 按区域识别图片中的文字
    ///
    /// @param file    上传的图片文件
    /// @param regions JSON格式的区域列表
    /// @return OCR识别结果
    @PostMapping(value = "/recognize", version = "1.0.0+")
    public OcrResult recognize(
            @RequestParam("file") MultipartFile file,
            @RequestPart("regions") List<OcrForm.Region> regions) throws IOException {

        log.info("{}收到OCR识别请求, 文件={}, 大小={}, 区域数={}", LogPrefix.OCR.p(),
                file.getOriginalFilename(), file.getSize(), regions.size());

        OcrForm form = new OcrForm(regions);
        return ocrService.recognize(file.getInputStream(), form);
    }
}
