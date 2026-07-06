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

package com.devops00.spectra.ocr.engine;

/// CTC贪心解码器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
public class CtcDecoder {

    /// 解码结果，包含文本和置信度
    public record DecodedResult(String text, float confidence) {
    }

    private CtcDecoder() {
    }

    /// CTC贪心解码
    ///
    /// @param logits      模型输出 [seqLen x vocabSize]
    /// @param vocabulary  词表（index 0 为blank）
    /// @return 解码结果
    public static DecodedResult decode(float[][] logits, String[] vocabulary) {
        StringBuilder sb = new StringBuilder();
        float totalScore = 0f;
        int count = 0;
        int prevIndex = -1;

        for (float[] timestep : logits) {
            int argmax = 0;
            float maxVal = timestep[0];
            for (int j = 1; j < timestep.length; j++) {
                if (timestep[j] > maxVal) {
                    maxVal = timestep[j];
                    argmax = j;
                }
            }
            if (argmax != 0 && argmax != prevIndex) {
                if (argmax < vocabulary.length) {
                    sb.append(vocabulary[argmax]);
                }
                totalScore += maxVal;
                count++;
            }
            prevIndex = argmax;
        }

        float confidence = count > 0 ? totalScore / count : 0f;
        return new DecodedResult(sb.toString(), confidence);
    }
}
