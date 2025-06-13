/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 树工具
 *
 * @author 杨新杰
 * @since 2025/6/13 10:03
 */
public final class TreeUtils {

    private TreeUtils() {
    }


    /**
     * 构建树形结构（参数 ≤5，符合团队规范）
     *
     * @param list   扁平数据列表
     * @param rootId 根节点ID（例如 "0"）
     * @param config 构建配置对象
     * @param <T>    实体类型
     * @return 树形结构列表
     */
    public static <T> List<TreeNode> buildTree(List<T> list, String rootId, TreeBuildConfig<T> config) {

        // 初始化配置项（带默认值）
        Function<T, String> idFunc = config.getIdFunc() != null ? config.getIdFunc() : item -> null;
        Function<T, String> parentIdFunc = config.getParentIdFunc() != null ? config.getParentIdFunc() : item -> null;
        BiConsumer<T, TreeNode> nodeMapper = config.getNodeMapper();
        Comparator<TreeNode> comparator = config.getComparator() != null ? config.getComparator() : TreeBuildConfig.defaultComparator();

        if (nodeMapper == null) {
            throw new IllegalArgumentException("nodeMapper 不能为空");
        }

        // 第一步：构建节点映射表
        Map<String, TreeNode> nodeMap = buildNodeMap(list, idFunc, parentIdFunc, nodeMapper);

        // 第二步：建立父子关系
        List<TreeNode> rootNodes = linkParentAndChildren(nodeMap, rootId, parentIdFunc, config.isSkipIfParentNotExists());

        // 第三步：递归排序
        sortTreeRecursively(rootNodes, comparator);

        return rootNodes;
    }

    // 构建节点映射表
    private static <T> Map<String, TreeNode> buildNodeMap(List<T> list,
                                                          Function<T, String> idFunc,
                                                          Function<T, String> parentIdFunc,
                                                          BiConsumer<T, TreeNode> nodeMapper) {

        Map<String, TreeNode> nodeMap = new HashMap<>();

        for (T item : list) {
            String id = idFunc.apply(item);
            if (id == null) {
                continue;
            }

            TreeNode node = new TreeNode();
            node.setId(id);
            node.setParentId(parentIdFunc.apply(item));
            nodeMapper.accept(item, node);

            nodeMap.put(id, node);
        }

        return nodeMap;
    }

    // 建立父子关系
    private static List<TreeNode> linkParentAndChildren(Map<String, TreeNode> nodeMap,
                                                        String rootId,
                                                        Function<?, String> parentIdFunc,
                                                        boolean skipIfParentNotExists) {

        List<TreeNode> rootNodes = new ArrayList<>();

        for (TreeNode node : nodeMap.values()) {
            String parentId = node.getParentId();

            if (parentId == null || Objects.equals(parentId, rootId)) {
                rootNodes.add(node);
            } else {
                TreeNode parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else if (!skipIfParentNotExists) {
                    rootNodes.add(node);
                }
            }
        }

        return rootNodes;
    }

    // 递归排序
    private static void sortTreeRecursively(List<TreeNode> nodes, Comparator<TreeNode> comparator) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(comparator);
        for (TreeNode node : nodes) {
            sortTreeRecursively(node.getChildren(), comparator);
        }

    }

}
