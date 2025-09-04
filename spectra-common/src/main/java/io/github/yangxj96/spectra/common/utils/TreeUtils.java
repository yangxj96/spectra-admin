package io.github.yangxj96.spectra.common.utils;

import io.github.yangxj96.spectra.common.base.javabean.vo.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 树形工具类
 */
public class TreeUtils {


    /**
     * 压缩选中的树节点：如果父节点的所有子节点都被选中，则只保留父节点
     *
     * @param tree        树的根节点列表
     * @param selectedIds 用户选中的节点ID集合
     * @param idExtractor 提取节点ID的函数（如 AuthorityTreeVO::getId）
     * @param <T>         实现 Tree<T> 的具体类型
     * @return 压缩处理后的选中ID集合
     */
    public static <T extends Tree<T>> Set<Long> compressSelectedNodes(List<T> tree, Set<Long> selectedIds, Function<T, Long> idExtractor) {
        Set<Long> result = new HashSet<>();
        for (T node : tree) {
            Set<Long> nodeResult = new HashSet<>();
            collectCompressedIds(node, selectedIds, idExtractor, nodeResult);
            result.addAll(nodeResult);
        }
        return result;
    }

    /**
     * 递归收集压缩后的节点ID
     */
    private static <T extends Tree<T>> boolean collectCompressedIds(T node, Set<Long> selectedIds, Function<T, Long> idExtractor, Set<Long> result) {

        if (node == null) return false;

        Long nodeId = idExtractor.apply(node);
        if (nodeId == null) return false;

        List<T> children = node.getChildren();

        // 叶子节点
        if (children == null || children.isEmpty()) {
            if (selectedIds.contains(nodeId)) {
                result.add(nodeId);
                return true;
            }
            return false;
        }

        // 非叶子节点：递归处理子节点
        boolean allChildrenSelected = true;
        List<Set<Long>> childResults = new ArrayList<>();

        for (T child : children) {
            Set<Long> childResult = new HashSet<>();
            boolean isSelected = collectCompressedIds(child, selectedIds, idExtractor, childResult);
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
        for (Set<Long> childResult : childResults) {
            result.addAll(childResult);
        }

        // 如果父节点被单独选中（但子节点未全选），也保留父节点
        if (selectedIds.contains(nodeId)) {
            result.add(nodeId);
        }

        return allChildrenSelected && selectedIds.contains(nodeId);
    }

}
