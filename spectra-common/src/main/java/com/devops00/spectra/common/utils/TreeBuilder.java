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
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;

/// 通用树结构构建器
///
/// @param <T>
///            实现了[`Tree`](io.github.yangxj96.spectra.common.base.javabean.vo.Tree)的子类
///            @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
public record TreeBuilder<T extends Tree<T>>(@Nullable List<T> dataList) {

    /// 构建树形结构
    ///
    /// @param rootPid
    ///            根节点的 pid 值（例如 -1L、0L）
    /// @return 树形结构列表
    public @Nullable List<T> buildTree(@Nullable UUID rootPid) {
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyList();
        }

        var nodeMap = new HashMap<UUID, T>();
        var rootNodes = new ArrayList<T>();

        // 第一步：放入 map
        for (T node : dataList) {
            nodeMap.put(node.getId(), node);
        }

        // 第二步：组装父子关系
        for (T node : dataList) {
            var parentId = node.getPid();

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

    /// 对每个层级进行排序（按 sort 字段）
    private @Nullable List<T> sortTree(@Nullable List<T> nodes) {
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

    /// 获取排序字段值（兼容不同 VO）
    private Integer getSortValue(T node) {
        try {
            // 反射获取 sort 字段（如果存在）
            return (Integer) node.getClass().getMethod("getSort").invoke(node);
        } catch (Exception e) {
            log.error("获取排序字段值失败:{}", e.getMessage(), e);
            return 0; // 默认无排序
        }
    }
}
