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

package com.devops00.spectra.ocr.service;

import com.devops00.spectra.ocr.model.OcrForm;
import com.devops00.spectra.ocr.model.OcrResult;

import java.io.InputStream;

/// OCR服务接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
public interface OcrService {

    /// 按区域识别图片中的文字
    ///
    /// @param imageStream 图片流
    /// @param form        识别请求（含各区域坐标）
    /// @return 识别结果
    OcrResult recognize(InputStream imageStream, OcrForm form);
}
