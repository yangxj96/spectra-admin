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

package com.devops00.spectra.framework.persistence.pagination;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 客户端排序只能在当前端点允许的字段集合内生效。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/08
 */
class PageFromSortTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"password", "id desc", "id;select 1", "(select 1)", "id--", "id,created_at", "other.id", " id ", "i d"})
    void expressionsAndUnregisteredColumnsAreRejected(String column) {
        var request = new PageFrom();
        request.setOrders(List.of(new PageOrderFrom(column, true)));
        var failure = assertThrows(IllegalArgumentException.class, () -> request.toPage(Map.of("id", "id")));
        assertEquals("不支持的分页排序字段", failure.getMessage());
    }

    @Test
    void convertedOrdersCannotBeChangedByRequestMutation() {
        var order = new PageOrderFrom("id", true);
        var orders = new ArrayList<>(List.of(order));
        var request = new PageFrom(15L, 1L, orders);
        var page = request.toPage(Map.of("id", "id"));
        order.setColumn("password");
        orders.clear();
        assertEquals(1, page.orders().size());
        assertEquals("id", page.orders().getFirst().getColumn());
    }

    @Test
    void nullAndEmptyOrdersKeepServerDefaultOrdering() {
        var request = new PageFrom();
        assertTrue(request.toPage(Map.of("id", "id")).orders().isEmpty());
        request.setOrders(List.of());
        assertTrue(request.toPage(Map.of("id", "id")).orders().isEmpty());
    }

    @Test
    void registeredFieldsPreserveDirectionAndResolveServerAliases() {
        var request = new PageFrom(20L, 2L, List.of(new PageOrderFrom("created_at", false), new PageOrderFrom("id", true)));
        var page = request.toPage(Map.of("created_at", "d.created_at", "id", "d.id"));
        assertEquals(2L, page.getCurrent());
        assertEquals(20L, page.getSize());
        assertEquals(List.of("d.created_at", "d.id"), page.orders().stream().map(OrderItem::getColumn).toList());
        assertFalse(page.orders().getFirst().isAsc());
        assertTrue(page.orders().getLast().isAsc());
    }

    @Test
    void nullItemDuplicateFieldsAndAliasDuplicatesAreRejected() {
        var request = new PageFrom();
        var orders = new ArrayList<PageOrderFrom>();
        orders.add(null);
        request.setOrders(orders);
        assertThrows(IllegalArgumentException.class, () -> request.toPage(Map.of("id", "id")));
        request.setOrders(List.of(new PageOrderFrom("id", true), new PageOrderFrom("id", false)));
        assertThrows(IllegalArgumentException.class, () -> request.toPage(Map.of("id", "id")));
        request.setOrders(List.of(new PageOrderFrom("id", true), new PageOrderFrom("identifier", false)));
        assertThrows(IllegalArgumentException.class, () -> request.toPage(Map.of("id", "id", "identifier", "id")));
    }

    @Test
    void invalidServerMappingIsRejected() {
        var request = new PageFrom(15L, 1L, List.of(new PageOrderFrom("id", true)));
        assertThrows(IllegalStateException.class, () -> request.toPage(null));
        assertThrows(IllegalStateException.class, () -> request.toPage(Map.of("id", "id desc")));
        assertThrows(IllegalArgumentException.class, () -> request.toPage(Map.of()));
    }

    @Test
    void fixedOrderRejectsNonemptyOrders() {
        var request = new PageFrom();
        request.requireUnsorted();
        request.setOrders(List.of());
        request.requireUnsorted();
        request.setOrders(List.of(new PageOrderFrom("id", true)));
        assertThrows(IllegalArgumentException.class, request::requireUnsorted);
    }

    @Test
    void concurrentConversionsDoNotShareMutableOrderItems() throws Exception {
        var request = new PageFrom(15L, 1L, List.of(new PageOrderFrom("id", true)));
        try (var executor = Executors.newFixedThreadPool(2)) {
            var tasks = IntStream.range(0, 32).mapToObj(index -> (Callable<String>) () -> {
                var page = request.toPage(Map.of("id", "safe_" + index));
                page.orders().getFirst().setAsc(false);
                return page.orders().getFirst().getColumn();
            }).toList();
            var results = executor.invokeAll(tasks);
            for (int index = 0; index < results.size(); index++) {
                assertEquals("safe_" + index, results.get(index).get());
            }
        }
        assertEquals("id", request.getOrders().getFirst().getColumn());
        assertTrue(request.getOrders().getFirst().isAsc());
    }
}
