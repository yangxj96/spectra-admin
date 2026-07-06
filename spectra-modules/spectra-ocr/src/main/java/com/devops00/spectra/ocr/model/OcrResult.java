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

/// OCR识别结果
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OcrResult {

    /// 识别到的每个文本块（含位置和所属队伍）
    private List<TextEntry> entries;

    /// 队伍列表
    private List<TeamEntry> teams;

    /// 布局类型: "columns" 或 "grid_5x5"
    private String layout;

    /// 总文本数量
    private int totalTexts;

    /// 队伍信息
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamEntry {

        /// 队伍编号
        private int teamId;

        /// 队员列表
        private List<String> members;
    }

    /// 文本块条目（含位置和队伍归属）
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TextEntry {

        /// 文本内容
        private String text;

        /// 所属队伍编号
        private int teamId;

        /// 中心点X坐标
        private float x;

        /// 中心点Y坐标
        private float y;

        /// 宽度
        private float width;

        /// 高度
        private float height;
    }
}
