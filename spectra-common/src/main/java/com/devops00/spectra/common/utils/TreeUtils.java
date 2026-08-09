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

package com.devops00.spectra.common.utils;

import com.devops00.spectra.common.base.javabean.vo.Tree;

import java.util.*;
import java.util.function.Function;

/// 树形工具类
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
public class TreeUtils {

    private TreeUtils() {
        // 工具类禁止实例化
    }

    /// 压缩选中的树节点：如果父节点的所有子节点都被选中，则只保留父节点
    ///
    /// @param tree        树的根节点列表
    /// @param selectedIds 用户选中的节点ID集合
    /// @param idExtractor 提取节点ID的函数（如 AuthorityTreeVO::getId）
    /// @param <T>         实现 Tree<T> 的具体类型
    /// @return 压缩处理后的选中ID集合
    public static <T extends Tree<T>> Set<UUID> compressSelectedNodes(List<T> tree, Set<UUID> selectedIds, Function<T, UUID> idExtractor) {
        var result = new HashSet<UUID>();
        for (T node : tree) {
            var nodeResult = new HashSet<UUID>();
            collectCompressedIds(node, selectedIds, idExtractor, nodeResult);
            result.addAll(nodeResult);
        }
        return result;
    }

    /// 递归收集压缩后的节点ID
    private static <T extends Tree<T>> boolean collectCompressedIds(T node, Set<UUID> selectedIds, Function<T, UUID> idExtractor, Set<UUID> result) {

        if (node == null)
            return false;

        var nodeId = idExtractor.apply(node);
        if (nodeId == null)
            return false;

        var children = node.getChildren();

        // 叶子节点
        if (children == null || children.isEmpty()) {
            if (selectedIds.contains(nodeId)) {
                result.add(nodeId);
                return true;
            }
            return false;
        }

        // 非叶子节点：递归处理子节点
        var allChildrenSelected = true;
        var childResults = new ArrayList<Set<UUID>>();

        for (T child : children) {
            var childResult = new HashSet<UUID>();
            var isSelected = collectCompressedIds(child, selectedIds, idExtractor, childResult);
            childResults.add(childResult);
            if (!isSelected) {
                allChildrenSelected = false;
            }
        }

        // 如果所有子节点都被选中，且父节点也被选中 → 只保留父节点
        if (allChildrenSelected && selectedIds.contains(nodeId)) {
            result.add(nodeId);
            return true;
        }

        // 否则，保留子节点的结果
        for (var childResult : childResults) {
            result.addAll(childResult);
        }

        // 如果父节点被单独选中（但子节点未全选），也保留父节点
        if (selectedIds.contains(nodeId)) {
            result.add(nodeId);
        }

        return allChildrenSelected && selectedIds.contains(nodeId);
    }
}
