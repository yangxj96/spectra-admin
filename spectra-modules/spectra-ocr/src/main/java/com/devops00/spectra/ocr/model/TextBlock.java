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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// OCR识别出的文本块
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextBlock {

    /// 识别出的文本内容
    private String text;

    /// 识别置信度
    private float score;

    /// 中心点X坐标
    private float centerX;

    /// 中心点Y坐标
    private float centerY;

    /// 4个角点坐标 [[x1,y1],[x2,y2],[x3,y3],[x4,y4]]
    private float[][] bbox;
}
