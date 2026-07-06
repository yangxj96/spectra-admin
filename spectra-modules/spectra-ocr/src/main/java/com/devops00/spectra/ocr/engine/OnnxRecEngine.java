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

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.ocr.model.OcrProperties;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.bytedeco.opencv.opencv_core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/// 文字识别引擎 (PaddleOCR CRNN)
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Component
public class OnnxRecEngine {

    private static final Logger log = LoggerFactory.getLogger(OnnxRecEngine.class);
    private static final int TARGET_HEIGHT = 48;
    private static final float[] MEAN = {0.5f, 0.5f, 0.5f};
    private static final float[] STD = {0.5f, 0.5f, 0.5f};

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String[] vocabulary;

    public OnnxRecEngine(OcrProperties properties) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        String recModelPath = properties.getRecModelPath();
        String dictPath = properties.getDictPath();
        log.info("{}初始化识别引擎, model={}, dict={}", LogPrefix.OCR.p(), recModelPath, dictPath);
        byte[] modelBytes = new ClassPathResource(recModelPath).getContentAsByteArray();
        this.session = env.createSession(modelBytes);
        this.vocabulary = loadDictionary(dictPath);
        log.info("{}识别引擎初始化完成, 词典大小={}", LogPrefix.OCR.p(), vocabulary.length);
    }

    /// 识别图像中的文字
    ///
    /// @param image 文字区域图像
    /// @return 解码结果（文本+置信度）
    public CtcDecoder.DecodedResult recognize(Mat image) {
        int h = image.rows();
        int w = image.cols();

        float ratio = (float) TARGET_HEIGHT / h;
        int targetWidth = Math.max(1, Math.round(w * ratio));
        int roundedWidth = Math.max(32, ((targetWidth + 31) / 32) * 32);

        Mat resized = new Mat();
        resize(image, resized, new Size(roundedWidth, TARGET_HEIGHT));

        float[] inputData = normalizeAndTranspose(resized);

        try {
            long[] shape = {1, 3, TARGET_HEIGHT, roundedWidth};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
            var inputName = session.getInputNames().iterator().next();
            var output = session.run(Collections.singletonMap(inputName, inputTensor));
            float[][][] logits = (float[][][]) output.get(0).getValue();
            return CtcDecoder.decode(logits[0], vocabulary);
        } catch (Exception e) {
            throw new RuntimeException("Rec inference failed", e);
        }
    }

    private float[] normalizeAndTranspose(Mat image) {
        int h = image.rows();
        int w = image.cols();
        float[] data = new float[3 * h * w];
        int channelSize = h * w;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                byte[] pixel = new byte[3];
                pixel[0] = image.ptr(y, x).get(0);
                pixel[1] = image.ptr(y, x).get(1);
                pixel[2] = image.ptr(y, x).get(2);

                float r = ((pixel[2] & 0xFF) / 255.0f - MEAN[0]) / STD[0];
                float g = ((pixel[1] & 0xFF) / 255.0f - MEAN[1]) / STD[1];
                float b = ((pixel[0] & 0xFF) / 255.0f - MEAN[2]) / STD[2];

                data[y * w + x] = r;
                data[channelSize + y * w + x] = g;
                data[2 * channelSize + y * w + x] = b;
            }
        }
        return data;
    }

    private String[] loadDictionary(String dictPath) {
        try {
            ClassPathResource resource = new ClassPathResource(dictPath);
            List<String> lines;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                lines = reader.lines().toList();
            }
            String[] dict = new String[lines.size() + 1];
            dict[0] = "";
            for (int i = 0; i < lines.size(); i++) {
                dict[i + 1] = lines.get(i);
            }
            return dict;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dictionary: " + dictPath, e);
        }
    }
}
