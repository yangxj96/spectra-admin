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

import com.yangxj96.spectra.core.base.javabean.vo.Tree;

import java.util.*;

/**
 * 通用树结构构建器
 */
public class TreeBuilder<T extends Tree<T>> {

    private final List<T> dataList;

    public TreeBuilder(List<T> dataList) {
        this.dataList = dataList;
    }

    /**
     * 构建树形结构
     *
     * @param rootPid 根节点的 pid 值（例如 -1L、0L）
     * @return 树形结构列表
     */
    public List<T> buildTree(Long rootPid) {
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, T> nodeMap = new HashMap<>();
        List<T> rootNodes = new ArrayList<>();

        // 第一步：放入 map
        for (T node : dataList) {
            nodeMap.put(node.getId(), node);
        }

        // 第二步：组装父子关系
        for (T node : dataList) {
            Long parentId = node.getPid();

            if (parentId == null || parentId.equals(rootPid)) {
                rootNodes.add(node);
            } else if (nodeMap.containsKey(parentId)) {
                T parent = nodeMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }

        return sortTree(rootNodes);
    }

    /**
     * 对每个层级进行排序（按 sort 字段）
     */
    private List<T> sortTree(List<T> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return nodes;
        }
        // 按 sort 排序（假设 T 中有 getSort 方法）
        nodes.sort(Comparator.comparing(this::getSortValue));
        for (T node : nodes) {
            sortTree(node.getChildren());
        }
        return nodes;
    }

    /**
     * 获取排序字段值（兼容不同 VO）
     */
    private Integer getSortValue(T node) {
        try {
            // 反射获取 sort 字段（如果存在）
            return (Integer) node.getClass().getMethod("getSort").invoke(node);
        } catch (Exception e) {
            return 0; // 默认无排序
        }
    }
}