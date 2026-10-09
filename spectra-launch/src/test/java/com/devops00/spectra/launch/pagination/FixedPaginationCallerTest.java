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

package com.devops00.spectra.launch.pagination;

import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.oa.contact.javabean.converter.ContactConverter;
import com.devops00.spectra.oa.contact.service.impl.ContactServiceImpl;
import com.devops00.spectra.workflow.service.impl.TaskServiceImpl;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 固定排序的公共调用方在未指定排序时继续执行原有分页流程。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/08
 */
class FixedPaginationCallerTest {
    @Test
    void workflowRetainsTaskAndHistoryOrderingForNullAndEmptyOrders() {
        var tasks = mock(TaskService.class);
        var history = mock(HistoryService.class);
        var taskQuery = mock(TaskQuery.class, RETURNS_SELF);
        var historyQuery = mock(HistoricTaskInstanceQuery.class, RETURNS_SELF);
        when(tasks.createTaskQuery()).thenReturn(taskQuery);
        when(history.createHistoricTaskInstanceQuery()).thenReturn(historyQuery);
        when(taskQuery.count()).thenReturn(0L);
        when(historyQuery.count()).thenReturn(0L);
        when(taskQuery.listPage(10, 10)).thenReturn(List.of());
        when(historyQuery.listPage(10, 10)).thenReturn(List.of());
        var service = new TaskServiceImpl(tasks, history, null, null, null, null, null, null);
        var request = new PageFrom(10L, 2L, null);
        assertTrue(service.todo(request, "synthetic-user", null).getRecords().isEmpty());
        request.setOrders(List.of());
        assertEquals(2L, service.done(request, "synthetic-user", null).getCurrent());
        verify(taskQuery).orderByTaskCreateTime();
        verify(taskQuery).desc();
        verify(historyQuery).orderByHistoricTaskInstanceEndTime();
        verify(historyQuery).desc();
        verify(taskQuery).listPage(10, 10);
        verify(historyQuery).listPage(10, 10);
    }

    @Test
    void contactAcceptsNullAndEmptyOrders() {
        var directory = mock(DirectoryQueryPort.class);
        var service = new ContactServiceImpl(directory, mock(ContactConverter.class));
        var request = new PageFrom();
        assertTrue(service.page(request, null).getRecords().isEmpty());
        request.setOrders(List.of());
        assertEquals(0, service.page(request, null).getTotal());
    }
}
