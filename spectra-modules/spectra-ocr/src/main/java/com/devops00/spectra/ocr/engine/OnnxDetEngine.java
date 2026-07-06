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
import org.bytedeco.opencv.global.opencv_imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// 文字检测引擎 (PaddleOCR DB Detector)
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Component
public class OnnxDetEngine {

    private static final Logger log = LoggerFactory.getLogger(OnnxDetEngine.class);
    private static final int CV_8UC1 = 0;

    private final OrtEnvironment env;
    private final OrtSession session;
    private final float detThresh;
    private final float detBoxThresh;
    private final float detUnclipRatio;

    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    public OnnxDetEngine(OcrProperties properties) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        String modelPath = properties.getDetModelPath();
        log.info("{}初始化检测引擎, model={}", LogPrefix.OCR.p(), modelPath);
        byte[] modelBytes = new ClassPathResource(modelPath).getContentAsByteArray();
        this.session = env.createSession(modelBytes);
        this.detThresh = properties.getDetThresh();
        this.detBoxThresh = properties.getDetBoxThresh();
        this.detUnclipRatio = properties.getDetUnclipRatio();
        log.info("{}检测引擎初始化完成, thresh={}, boxThresh={}, unclip={}",
                LogPrefix.OCR.p(), detThresh, detBoxThresh, detUnclipRatio);
    }

    /// 检测文字区域，返回旋转矩形框
    ///
    /// @param image 输入图像
    /// @return 边界框列表，每个边界框为4个角点
    public List<float[][]> detect(Mat image) {
        int h = image.rows();
        int w = image.cols();
        int ratioH = roundTo32(h);
        int ratioW = roundTo32(w);

        log.debug("{}检测: 原始={}x{}, 缩放={}x{}", LogPrefix.OCR.p(), w, h, ratioW, ratioH);

        Mat resized = new Mat();
        opencv_imgproc.resize(image, resized, new Size(ratioW, ratioH));

        float[] inputData = normalizeAndTranspose(resized);

        try {
            long[] shape = {1, 3, ratioH, ratioW};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
            var inputName = session.getInputNames().iterator().next();
            var output = session.run(Collections.singletonMap(inputName, inputTensor));
            float[][][][] probMap = (float[][][][]) output.get(0).getValue();
            float[][] prob = probMap[0][0];

            // 调试：概率图统计
            float minProb = Float.MAX_VALUE;
            float maxProb = -Float.MAX_VALUE;
            long aboveCount = 0;
            for (int y = 0; y < prob.length; y++) {
                for (int x = 0; x < prob[0].length; x++) {
                    float p = prob[y][x];
                    if (p < minProb) minProb = p;
                    if (p > maxProb) maxProb = p;
                    if (p > detThresh) aboveCount++;
                }
            }
            log.info("{}概率图: {}x{}, min={}, max={}, 阈值({})以上={}像素",
                    LogPrefix.OCR.p(), prob[0].length, prob.length,
                    String.format("%.4f", minProb), String.format("%.4f", maxProb),
                    detThresh, aboveCount);

            return postProcess(prob, h, w, ratioH, ratioW);
        } catch (Exception e) {
            throw new RuntimeException("Det inference failed", e);
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
                // BGR order from OpenCV
                pixel[0] = image.ptr(y, x).get(0);
                pixel[1] = image.ptr(y, x).get(1);
                pixel[2] = image.ptr(y, x).get(2);

                // RGB, normalize, CHW
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

    private int roundTo32(int val) {
        return Math.max(32, ((val + 31) / 32) * 32);
    }

    private List<float[][]> postProcess(float[][] probMap, int origH, int origW, int ratioH, int ratioW) {
        int h = probMap.length;
        int w = probMap[0].length;

        Mat binary = new Mat(h, w, CV_8UC1);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                binary.ptr(y, x).put((byte) (probMap[y][x] > detThresh ? 255 : 0));
            }
        }

        Mat kernel = opencv_imgproc.getStructuringElement(opencv_imgproc.MORPH_RECT, new Size(2, 2));
        Mat dilated = new Mat();
        opencv_imgproc.dilate(binary, dilated, kernel);

        MatVector contours = new MatVector();
        opencv_imgproc.findContours(dilated, contours, opencv_imgproc.RETR_LIST, opencv_imgproc.CHAIN_APPROX_SIMPLE);

        log.info("{}找到 {} 个轮廓", LogPrefix.OCR.p(), contours.size());

        List<float[][]> boxes = new ArrayList<>();
        float scaleX = (float) origW / ratioW;
        float scaleY = (float) origH / ratioH;
        int quadCount = 0;

        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = opencv_imgproc.contourArea(contour);
            if (area < 3) {
                continue;
            }

            // 使用 minAreaRect 获取最小外接旋转矩形（始终返回4个点）
            RotatedRect rotRect = opencv_imgproc.minAreaRect(contour);
            Point2f vertices = new Point2f(4);
            rotRect.points(vertices);

            quadCount++;

            double score = boxScore(probMap, rotRect);
            if (score < detBoxThresh) {
                continue;
            }

            float[][] box = new float[4][2];
            for (int j = 0; j < 4; j++) {
                box[j][0] = vertices.position(j).x() * scaleX;
                box[j][1] = vertices.position(j).y() * scaleY;
            }
            box = unclipBox(box, detUnclipRatio);
            boxes.add(box);
        }
        log.info("{}旋转矩形: {}, 通过置信度: {}", LogPrefix.OCR.p(), quadCount, boxes.size());
        return boxes;
    }

    private double boxScore(float[][] probMap, RotatedRect rotRect) {
        int h = probMap.length;
        int w = probMap[0].length;
        Rect rect = rotRect.boundingRect();
        int x1 = Math.max(0, rect.x());
        int y1 = Math.max(0, rect.y());
        int x2 = Math.min(w - 1, rect.x() + rect.width());
        int y2 = Math.min(h - 1, rect.y() + rect.height());

        double sum = 0;
        int count = 0;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                sum += probMap[y][x];
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private float[][] unclipBox(float[][] box, float ratio) {
        float[] center = new float[2];
        for (float[] pt : box) {
            center[0] += pt[0];
            center[1] += pt[1];
        }
        center[0] /= 4;
        center[1] /= 4;

        float maxDist = 0;
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            float dx = box[j][0] - box[i][0];
            float dy = box[j][1] - box[i][1];
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > maxDist) {
                maxDist = dist;
            }
        }

        float clipDist = maxDist * ratio / 2;
        float[][] result = new float[4][2];
        for (int i = 0; i < 4; i++) {
            float dx = box[i][0] - center[0];
            float dy = box[i][1] - center[1];
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len == 0) {
                result[i][0] = box[i][0];
                result[i][1] = box[i][1];
            } else {
                float scale = (len + clipDist) / len;
                result[i][0] = center[0] + dx * scale;
                result[i][1] = center[1] + dy * scale;
            }
        }
        return result;
    }
}
