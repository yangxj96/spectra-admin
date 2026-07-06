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

package com.devops00.spectra.ocr.service.impl;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.ocr.engine.ColumnClusterer;
import com.devops00.spectra.ocr.engine.CtcDecoder;
import com.devops00.spectra.ocr.engine.OnnxDetEngine;
import com.devops00.spectra.ocr.engine.OnnxRecEngine;
import com.devops00.spectra.ocr.model.OcrResult;
import com.devops00.spectra.ocr.model.TextBlock;
import com.devops00.spectra.ocr.service.OcrService;
import org.bytedeco.opencv.opencv_core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;

/// OCR服务实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Service
public class OcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrServiceImpl.class);

    private final OnnxDetEngine detEngine;
    private final OnnxRecEngine recEngine;
    private final ColumnClusterer columnClusterer;

    public OcrServiceImpl(OnnxDetEngine detEngine, OnnxRecEngine recEngine, ColumnClusterer columnClusterer) {
        this.detEngine = detEngine;
        this.recEngine = recEngine;
        this.columnClusterer = columnClusterer;
    }

    @Override
    public OcrResult recognize(InputStream imageStream) {
        try {
            byte[] imageBytes = imageStream.readAllBytes();
            Mat dataMat = new Mat(imageBytes);
            Mat image = imdecode(dataMat, IMREAD_COLOR);

            log.info("{}开始识别, 图片尺寸={}x{}", LogPrefix.OCR.p(), image.cols(), image.rows());

            List<float[][]> boxes = detEngine.detect(image);
            log.info("{}检测到 {} 个文本区域", LogPrefix.OCR.p(), boxes.size());

            List<TextBlock> textBlocks = new ArrayList<>();
            for (float[][] box : boxes) {
                Mat cropped = cropRegion(image, box);
                if (cropped == null || cropped.empty()) {
                    continue;
                }

                CtcDecoder.DecodedResult result = recEngine.recognize(cropped);

                float cx = (box[0][0] + box[1][0] + box[2][0] + box[3][0]) / 4;
                float cy = (box[0][1] + box[1][1] + box[2][1] + box[3][1]) / 4;

                textBlocks.add(new TextBlock(
                        result.text(),
                        result.confidence(),
                        cx,
                        cy,
                        box
                ));
            }

            log.info("{}识别完成, 共 {} 个文本", LogPrefix.OCR.p(), textBlocks.size());

            OcrResult result = columnClusterer.cluster(textBlocks, image.cols(), image.rows());
            log.info("{}聚类完成, {} 支队伍, 布局={}", LogPrefix.OCR.p(),
                    result.getTeams().size(), result.getLayout());

            return result;
        } catch (Exception e) {
            log.error("{}OCR识别失败", LogPrefix.OCR.p(), e);
            throw new RuntimeException("OCR recognition failed", e);
        }
    }

    private Mat cropRegion(Mat image, float[][] box) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (float[] pt : box) {
            minX = Math.min(minX, pt[0]);
            minY = Math.min(minY, pt[1]);
            maxX = Math.max(maxX, pt[0]);
            maxY = Math.max(maxY, pt[1]);
        }

        int x1 = Math.max(0, Math.round(minX));
        int y1 = Math.max(0, Math.round(minY));
        int x2 = Math.min(image.cols() - 1, Math.round(maxX));
        int y2 = Math.min(image.rows() - 1, Math.round(maxY));

        if (x2 <= x1 || y2 <= y1) {
            return null;
        }

        Rect roi = new Rect(x1, y1, x2 - x1, y2 - y1);
        return new Mat(image, roi).clone();
    }
}
