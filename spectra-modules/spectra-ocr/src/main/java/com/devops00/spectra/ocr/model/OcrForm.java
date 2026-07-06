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

import java.util.List;

/// OCR识别请求：图片 + 各队伍的识别区域
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OcrForm {

    /// 各队伍的识别区域列表
    private List<Region> regions;

    /// 识别区域
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Region {

        /// 队伍编号
        private int teamId;

        /// 区域左上角X（图片坐标）
        private float x;

        /// 区域左上角Y（图片坐标）
        private float y;

        /// 区域宽度
        private float width;

        /// 区域高度
        private float height;
    }
}
