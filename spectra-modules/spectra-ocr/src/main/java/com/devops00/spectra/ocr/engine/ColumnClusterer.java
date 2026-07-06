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

import com.devops00.spectra.ocr.model.OcrResult;
import com.devops00.spectra.ocr.model.TextBlock;
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

    /// 将文本块按空间位置聚类为队伍
    ///
    /// @param textBlocks  OCR识别出的文本块列表
    /// @param imgWidth    图像宽度
    /// @param imgHeight   图像高度
    /// @return 聚类后的OCR结果
    public OcrResult cluster(List<TextBlock> textBlocks, int imgWidth, int imgHeight) {
        List<TextBlock> filtered = textBlocks.stream()
                .filter(tb -> !isDigitHeader(tb.getText()))
                .toList();

        if (filtered.isEmpty()) {
            return new OcrResult(List.of(), List.of(), "columns", 0);
        }

        float yGap = findMaxYGap(filtered, imgHeight);
        List<TextBlock> topGroup = new ArrayList<>();
        List<TextBlock> bottomGroup = new ArrayList<>();

        for (TextBlock tb : filtered) {
            if (tb.getCenterY() < yGap) {
                topGroup.add(tb);
            } else {
                bottomGroup.add(tb);
            }
        }

        List<OcrResult.TeamEntry> teams = new ArrayList<>();
        List<OcrResult.TextEntry> entries = new ArrayList<>();
        float xThreshold = imgWidth * 0.06f;

        // 上排
        List<List<TextBlock>> topClusters = clusterByX(topGroup, xThreshold);
        for (int i = 0; i < topClusters.size(); i++) {
            int teamId = i + 1;
            List<TextBlock> cluster = topClusters.get(i);
            List<String> members = cluster.stream()
                    .sorted(Comparator.comparingDouble(TextBlock::getCenterY))
                    .map(TextBlock::getText)
                    .toList();
            teams.add(new OcrResult.TeamEntry(teamId, members));

            // 为每个文本块生成 TextEntry
            for (TextBlock tb : cluster) {
                entries.add(buildTextEntry(tb, teamId));
            }
        }

        // 下排
        List<List<TextBlock>> bottomClusters = clusterByX(bottomGroup, xThreshold);
        for (int i = 0; i < bottomClusters.size(); i++) {
            int teamId = 6 + i;
            List<TextBlock> cluster = bottomClusters.get(i);
            List<String> members = cluster.stream()
                    .sorted(Comparator.comparingDouble(TextBlock::getCenterY))
                    .map(TextBlock::getText)
                    .toList();
            teams.add(new OcrResult.TeamEntry(teamId, members));

            for (TextBlock tb : cluster) {
                entries.add(buildTextEntry(tb, teamId));
            }
        }

        return new OcrResult(entries, teams, "columns", filtered.size());
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

    private OcrResult.TextEntry buildTextEntry(TextBlock tb, int teamId) {
        float[][] bbox = tb.getBbox();
        float x, y, w, h;

        if (bbox != null && bbox.length == 4) {
            // bbox 是 4 个角点 [[x1,y1],[x2,y2],[x3,y3],[x4,y4]]
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (float[] pt : bbox) {
                minX = Math.min(minX, pt[0]);
                minY = Math.min(minY, pt[1]);
                maxX = Math.max(maxX, pt[0]);
                maxY = Math.max(maxY, pt[1]);
            }
            x = minX;
            y = minY;
            w = maxX - minX;
            h = maxY - minY;
        } else {
            // fallback: 以 center 为中心，估算尺寸
            w = Math.max(40, tb.getText().length() * 14);
            h = 20;
            x = tb.getCenterX() - w / 2;
            y = tb.getCenterY() - h / 2;
        }

        return new OcrResult.TextEntry(tb.getText(), teamId, x, y, w, h);
    }
}
