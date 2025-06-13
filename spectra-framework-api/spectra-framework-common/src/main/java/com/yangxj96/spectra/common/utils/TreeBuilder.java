package com.yangxj96.spectra.common.utils;


import com.yangxj96.spectra.common.base.javabean.vo.Tree;

import java.util.*;

/**
 * 通用树结构构建器
 *
 * @param <T> 实现了{@link Tree}的子类
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
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
