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

package com.devops00.spectra.workflow.service.impl;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Event;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 为没有 BPMN-DI 的历史流程模型补齐最小可渲染布局。
 */
final class BpmnDiagramSupport {

    private BpmnDiagramSupport() {
    }

    /**
     * 处理内部业务逻辑（{@code ensureGraphicInfo}）。
     */
    static void ensureGraphicInfo(BpmnModel model) {
        Process process = model.getMainProcess();
        if (process == null) {
            return;
        }

        List<FlowNode> nodes = process.getFlowElements()
                .stream()
                .filter(FlowNode.class::isInstance)
                .map(FlowNode.class::cast)
                .toList();
        if (nodes.isEmpty()) {
            return;
        }

        Map<String, Integer> depths = calculateDepths(nodes, process);
        Map<Integer, List<FlowNode>> levels = new LinkedHashMap<>();
        nodes.stream()
                .sorted(Comparator.comparingInt(node -> depths.getOrDefault(node.getId(), 0)))
                .forEach(node -> levels.computeIfAbsent(depths.getOrDefault(node.getId(), 0), ignored -> new ArrayList<>()).add(node));

        Map<String, GraphicInfo> locations = new HashMap<>();
        levels.forEach((depth, levelNodes) -> {
            for (int index = 0; index < levelNodes.size(); index++) {
                FlowNode node = levelNodes.get(index);
                double width = node instanceof Event ? 36 : node instanceof Gateway ? 44 : 130;
                double height = node instanceof Event ? 36 : node instanceof Gateway ? 44 : 70;
                GraphicInfo info = model.getGraphicInfo(node.getId());
                if (info == null) {
                    info = new GraphicInfo(80 + depth * 190, 80 + index * 120, width, height);
                    model.addGraphicInfo(node.getId(), info);
                }
                locations.put(node.getId(), info);
            }
        });

        for (FlowNode node : nodes) {
            for (SequenceFlow flow : node.getOutgoingFlows()) {
                var target = flow.getTargetFlowElement();
                if (target == null && flow.getTargetRef() != null) {
                    target = process.getFlowElement(flow.getTargetRef());
                }
                GraphicInfo sourceInfo = locations.get(node.getId());
                GraphicInfo targetInfo = target == null ? null : locations.get(target.getId());
                if (sourceInfo == null || targetInfo == null || !model.getFlowLocationMap().getOrDefault(flow.getId(), List.of()).isEmpty()) {
                    continue;
                }
                model.addFlowGraphicInfoList(flow.getId(), List.of(
                        new GraphicInfo(sourceInfo.getX() + sourceInfo.getWidth(), sourceInfo.getY() + sourceInfo.getHeight() / 2),
                        new GraphicInfo(targetInfo.getX(), targetInfo.getY() + targetInfo.getHeight() / 2)));
            }
        }
    }

    /**
     * 处理内部业务逻辑（{@code calculateDepths}）。
     */
    private static Map<String, Integer> calculateDepths(List<FlowNode> nodes, Process process) {
        Map<String, Integer> depths = new HashMap<>();
        nodes.forEach(node -> depths.put(node.getId(), 0));
        for (int pass = 0; pass < nodes.size(); pass++) {
            for (FlowNode node : nodes) {
                int depth = depths.getOrDefault(node.getId(), 0);
                for (SequenceFlow flow : node.getOutgoingFlows()) {
                    var target = flow.getTargetFlowElement();
                    if (target == null && flow.getTargetRef() != null) {
                        target = process.getFlowElement(flow.getTargetRef());
                    }
                    if (target instanceof FlowNode targetNode) {
                        depths.merge(targetNode.getId(), depth + 1, Math::max);
                    }
                }
            }
        }
        return depths;
    }
}
