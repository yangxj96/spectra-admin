/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.common.tree;

import com.devops00.spectra.common.base.javabean.vo.Tree;
import com.devops00.spectra.common.foundation.tree.TreeBuilder;
import com.devops00.spectra.common.foundation.tree.TreeUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 纯树结构能力的行为契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
class TreeContractTest {

    @Test
    void builderMustReturnEmptyListForNullOrEmptyInput() {
        assertThat(new TreeBuilder<Node>(null).buildTree(null)).isEmpty();
        assertThat(new TreeBuilder<Node>(List.of()).buildTree(null)).isEmpty();
    }

    @Test
    void builderMustCopyInputListAndAssembleAndSortRootsAndChildren() {
        UUID rootId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        UUID secondChildId = UUID.randomUUID();
        var root = new Node(rootId, null, 2);
        var child = new Node(childId, rootId, 3);
        var secondChild = new Node(secondChildId, rootId, 1);
        var input = new ArrayList<>(List.of(root, child, secondChild));

        var builder = new TreeBuilder<>(input);
        input.clear();

        assertThat(builder.buildTree(null)).containsExactly(root);
        assertThat(root.getChildren()).containsExactly(secondChild, child);
    }

    @Test
    void builderMustSkipNodesWhoseParentIsMissing() {
        var root = new Node(UUID.randomUUID(), null, 1);
        var orphan = new Node(UUID.randomUUID(), UUID.randomUUID(), 1);

        assertThat(new TreeBuilder<>(List.of(root, orphan)).buildTree(null))
                .containsExactly(root);
        assertThat(root.getChildren()).isNullOrEmpty();
    }

    @Test
    void builderMustNotDuplicateChildrenWhenCalledAgain() {
        UUID rootId = UUID.randomUUID();
        var root = new Node(rootId, null, 1);
        var child = new Node(UUID.randomUUID(), rootId, 1);
        var builder = new TreeBuilder<>(List.of(root, child));

        builder.buildTree(null);
        builder.buildTree(null);

        assertThat(root.getChildren()).containsExactly(child);
    }

    @Test
    void treeUtilsMustCompressFullySelectedBranchesButKeepPartialSelections() {
        UUID rootId = UUID.randomUUID();
        UUID firstChildId = UUID.randomUUID();
        UUID secondChildId = UUID.randomUUID();
        var root = new Node(rootId, null, 1);
        var firstChild = new Node(firstChildId, rootId, 1);
        var secondChild = new Node(secondChildId, rootId, 2);
        root.setChildren(new ArrayList<>(List.of(firstChild, secondChild)));

        assertThat(TreeUtils.compressSelectedNodes(List.of(root), Set.of(rootId, firstChildId, secondChildId), Node::getId))
                .containsExactly(rootId);
        assertThat(TreeUtils.compressSelectedNodes(List.of(root), Set.of(rootId, firstChildId), Node::getId))
                .containsExactlyInAnyOrder(rootId, firstChildId);
    }

    public static final class Node implements Tree<Node> {

        private UUID id;

        private UUID pid;

        private final int sort;

        private List<Node> children;

        private Node(UUID id, UUID pid, int sort) {
            this.id = id;
            this.pid = pid;
            this.sort = sort;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public void setId(UUID id) {
            this.id = id;
        }

        @Override
        public UUID getPid() {
            return pid;
        }

        @Override
        public void setPid(UUID pid) {
            this.pid = pid;
        }

        @Override
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<Node> children) {
            this.children = children;
        }

        public int getSort() {
            return sort;
        }
    }
}
