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
import com.devops00.spectra.ocr.model.OcrResult;
import com.devops00.spectra.ocr.model.TextBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// 列聚类器，将OCR识别结果按空间位置分配到队伍
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/6 12:00
@Component
public class ColumnClusterer {

    private static final Logger log = LoggerFactory.getLogger(ColumnClusterer.class);

    /// 将文本块按空间位置聚类为队伍
    public OcrResult cluster(List<TextBlock> textBlocks, int imgWidth, int imgHeight) {
        List<TextBlock> filtered = textBlocks.stream()
                .filter(tb -> !isDigitHeader(tb.getText()))
                .toList();

        int removedDigits = textBlocks.size() - filtered.size();
        log.info("{}聚类: 输入{}个文本, 过滤数字标题{}个, 剩余{}个",
                LogPrefix.OCR.p(), textBlocks.size(), removedDigits, filtered.size());

        if (filtered.isEmpty()) {
            return new OcrResult(List.of(), "columns", 0);
        }

        float yGap = findMaxYGap(filtered, imgHeight);
        log.info("{}Y分界线: {}", LogPrefix.OCR.p(), String.format("%.0f", yGap));

        List<TextBlock> topGroup = new ArrayList<>();
        List<TextBlock> bottomGroup = new ArrayList<>();

        for (TextBlock tb : filtered) {
            if (tb.getCenterY() < yGap) {
                topGroup.add(tb);
            } else {
                bottomGroup.add(tb);
            }
        }

        log.info("{}上排: {}个文本, 下排: {}个文本", LogPrefix.OCR.p(), topGroup.size(), bottomGroup.size());

        List<OcrResult.TeamEntry> teams = new ArrayList<>();
        float xThreshold = imgWidth * 0.06f;

        // 上排
        List<List<TextBlock>> topClusters = clusterByX(topGroup, xThreshold);
        log.info("{}上排聚类: {} 个队伍", LogPrefix.OCR.p(), topClusters.size());
        for (int i = 0; i < topClusters.size(); i++) {
            List<String> members = topClusters.get(i).stream()
                    .sorted(Comparator.comparingDouble(TextBlock::getCenterY))
                    .map(TextBlock::getText)
                    .toList();
            log.info("{}  队伍{}: {} 人 - {}", LogPrefix.OCR.p(), i + 1, members.size(), members);
            teams.add(new OcrResult.TeamEntry(i + 1, members));
        }

        // 下排
        List<List<TextBlock>> bottomClusters = clusterByX(bottomGroup, xThreshold);
        log.info("{}下排聚类: {} 个队伍", LogPrefix.OCR.p(), bottomClusters.size());
        for (int i = 0; i < bottomClusters.size(); i++) {
            List<String> members = bottomClusters.get(i).stream()
                    .sorted(Comparator.comparingDouble(TextBlock::getCenterY))
                    .map(TextBlock::getText)
                    .toList();
            log.info("{}  队伍{}: {} 人 - {}", LogPrefix.OCR.p(), 6 + i, members.size(), members);
            teams.add(new OcrResult.TeamEntry(6 + i, members));
        }

        return new OcrResult(teams, "columns", filtered.size());
    }

    private boolean isDigitHeader(String text) {
        return text.matches("^\\d{1,2}$");
    }

    private float findMaxYGap(List<TextBlock> blocks, int imgHeight) {
        List<Float> yCoords = blocks.stream()
                .map(TextBlock::getCenterY)
                .sorted()
                .toList();

        float maxGap = 0;
        float gapCenter = imgHeight / 2f;

        for (int i = 1; i < yCoords.size(); i++) {
            float gap = yCoords.get(i) - yCoords.get(i - 1);
            if (gap > maxGap) {
                maxGap = gap;
                gapCenter = (yCoords.get(i) + yCoords.get(i - 1)) / 2;
            }
        }
        return gapCenter;
    }

    private List<List<TextBlock>> clusterByX(List<TextBlock> blocks, float threshold) {
        if (blocks.isEmpty()) {
            return List.of();
        }

        List<TextBlock> sorted = blocks.stream()
                .sorted(Comparator.comparingDouble(TextBlock::getCenterX))
                .toList();

        List<List<TextBlock>> clusters = new ArrayList<>();
        List<TextBlock> currentCluster = new ArrayList<>();
        currentCluster.add(sorted.get(0));

        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).getCenterX() - sorted.get(i - 1).getCenterX() <= threshold) {
                currentCluster.add(sorted.get(i));
            } else {
                clusters.add(currentCluster);
                currentCluster = new ArrayList<>();
                currentCluster.add(sorted.get(i));
            }
        }
        clusters.add(currentCluster);
        return clusters;
    }
}
