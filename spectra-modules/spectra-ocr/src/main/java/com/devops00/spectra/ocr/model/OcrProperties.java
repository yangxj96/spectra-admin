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

package com.devops00.spectra.ocr.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/// OCR模块配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Data
@ConfigurationProperties(prefix = "spectra.ocr")
public class OcrProperties {

    /// 文字检测模型路径
    private String detModelPath = "models/det.onnx";

    /// 文字识别模型路径
    private String recModelPath = "models/rec.onnx";

    /// 识别字典路径
    private String dictPath = "models/ppocrv6_dict.txt";

    /// 检测置信度阈值
    private float detThresh = 0.2f;

    /// 检测框置信度阈值
    private float detBoxThresh = 0.4f;

    /// 检测框扩展比例
    private float detUnclipRatio = 1.4f;
}
