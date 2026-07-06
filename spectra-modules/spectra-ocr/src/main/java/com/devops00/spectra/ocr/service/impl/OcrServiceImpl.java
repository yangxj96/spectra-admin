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
import com.devops00.spectra.ocr.engine.CtcDecoder;
import com.devops00.spectra.ocr.engine.OnnxDetEngine;
import com.devops00.spectra.ocr.engine.OnnxRecEngine;
import com.devops00.spectra.ocr.model.OcrForm;
import com.devops00.spectra.ocr.model.OcrResult;
import com.devops00.spectra.ocr.service.OcrService;
import org.bytedeco.opencv.opencv_core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;

/// OCR服务实现：按用户指定区域进行识别
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Service
public class OcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrServiceImpl.class);

    private final OnnxDetEngine detEngine;
    private final OnnxRecEngine recEngine;

    public OcrServiceImpl(OnnxDetEngine detEngine, OnnxRecEngine recEngine) {
        this.detEngine = detEngine;
        this.recEngine = recEngine;
    }

    @Override
    public OcrResult recognize(InputStream imageStream, OcrForm form) {
        try {
            byte[] imageBytes = imageStream.readAllBytes();
            Mat dataMat = new Mat(imageBytes);
            Mat image = imdecode(dataMat, IMREAD_COLOR);

            log.info("{}开始识别, 图片尺寸={}x{}, 区域数={}",
                    LogPrefix.OCR.p(), image.cols(), image.rows(), form.getRegions().size());

            List<OcrResult.TeamEntry> teams = new ArrayList<>();
            int totalTexts = 0;

            for (OcrForm.Region region : form.getRegions()) {
                // 1. 裁剪区域
                Mat cropped = cropRegion(image, region);
                if (cropped == null || cropped.empty()) {
                    log.warn("{}区域 {} 裁剪为空, 跳过", LogPrefix.OCR.p(), region.getTeamId());
                    teams.add(new OcrResult.TeamEntry(region.getTeamId(), List.of()));
                    continue;
                }

                log.debug("{}区域 {}: 裁剪尺寸={}x{}", LogPrefix.OCR.p(), region.getTeamId(),
                        cropped.cols(), cropped.rows());

                // 2. 在裁剪区域内检测文本
                List<float[][]> boxes = detEngine.detect(cropped);
                log.debug("{}区域 {}: 检测到 {} 个文本", LogPrefix.OCR.p(), region.getTeamId(), boxes.size());

                // 3. 识别每个文本，同时保存位置信息
                List<float[]> centerYList = new ArrayList<>();
                List<String> textList = new ArrayList<>();

                for (float[][] box : boxes) {
                    Mat textImg = cropBox(cropped, box);
                    if (textImg == null || textImg.empty()) {
                        continue;
                    }

                    CtcDecoder.DecodedResult result = recEngine.recognize(textImg);
                    if (!result.text().isBlank()) {
                        float cy = (box[0][1] + box[1][1] + box[2][1] + box[3][1]) / 4;
                        centerYList.add(new float[]{cy});
                        textList.add(result.text());
                        log.debug("{}区域 {}: 识别到 '{}' (score={})",
                                LogPrefix.OCR.p(), region.getTeamId(),
                                result.text(), String.format("%.3f", result.confidence()));
                    }
                }

                // 4. 按 Y 坐标升序排序（从上到下）
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < textList.size(); i++) indices.add(i);
                indices.sort(Comparator.comparingDouble(i -> centerYList.get(i)[0]));

                List<String> members = indices.stream()
                        .map(textList::get)
                        .toList();

                totalTexts += members.size();
                teams.add(new OcrResult.TeamEntry(region.getTeamId(), members));
            }

            log.info("{}识别完成, 共 {} 支队伍, {} 个文本", LogPrefix.OCR.p(), teams.size(), totalTexts);

            return new OcrResult(List.of(), teams, "columns", totalTexts);
        } catch (Exception e) {
            log.error("{}OCR识别失败", LogPrefix.OCR.p(), e);
            throw new RuntimeException("OCR recognition failed", e);
        }
    }

    /// 按区域裁剪图片
    private Mat cropRegion(Mat image, OcrForm.Region region) {
        int x1 = Math.max(0, Math.round(region.getX()));
        int y1 = Math.max(0, Math.round(region.getY()));
        int x2 = Math.min(image.cols(), Math.round(region.getX() + region.getWidth()));
        int y2 = Math.min(image.rows(), Math.round(region.getY() + region.getHeight()));

        if (x2 <= x1 || y2 <= y1) {
            return null;
        }

        Rect roi = new Rect(x1, y1, x2 - x1, y2 - y1);
        return new Mat(image, roi).clone();
    }

    /// 按检测框裁剪文本区域
    private Mat cropBox(Mat image, float[][] box) {
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
