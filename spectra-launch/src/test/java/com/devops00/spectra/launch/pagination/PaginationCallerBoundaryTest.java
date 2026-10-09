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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.persistence.pagination.PageOrderFrom;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * 清单覆盖 Core、OA 与 Workflow 全部分页入口的排序边界。
 * 依赖未装配，非法排序必须在业务读取前被真实方法拒绝；合法转换单独捕获验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/08
 */
class PaginationCallerBoundaryTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("targets")
    void rejectsUnregisteredSortBeforeBusinessReads(Target target) throws Exception {
        var entry = target.entry();
        var request = newRequest(entry);
        var service = mock(target.type(), CALLS_REAL_METHODS);
        for (String field : List.of("password", "id--", "id;select 1")) {
            request.setOrders(List.of(new PageOrderFrom(field, true)));
            var failure = invokeFailure(entry, service, request);
            assertInstanceOf(IllegalArgumentException.class, failure);
            assertEquals("不支持的分页排序字段", failure.getMessage());
        }
        if (target.fields().isEmpty()) {
            request.setOrders(List.of(new PageOrderFrom("id", true)));
            assertInstanceOf(IllegalArgumentException.class, invokeFailure(entry, service, request));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sqlTargets")
    void registeredFieldsUseTheDeclaredSqlColumns(Target target) throws Exception {
        var entry = target.entry();
        var request = spy(newRequest(entry));
        var service = mock(target.type(), CALLS_REAL_METHODS);
        doAnswer(invocation -> {
            Map<String, String> fields = invocation.getArgument(0);
            assertEquals(target.fields(), fields);
            throw new ConvertedPage(new PageFrom(15L, 1L, request.getOrders()).toPage(fields));
        }).when(request).toPage(anyMap());
        for (var field : target.fields().entrySet()) {
            request.setOrders(List.of(new PageOrderFrom(field.getKey(), false)));
            var conversion = assertInstanceOf(ConvertedPage.class, invokeFailure(entry, service, request));
            assertEquals(field.getValue(), conversion.page.orders().getFirst().getColumn());
            assertFalse(conversion.page.orders().getFirst().isAsc());
        }
    }

    private static PageFrom newRequest(Method entry) throws Exception {
        var type = Arrays.stream(entry.getParameterTypes()).filter(PageFrom.class::isAssignableFrom).findFirst().orElseThrow();
        return (PageFrom) type.getDeclaredConstructor().newInstance();
    }

    private static Throwable invokeFailure(Method entry, Object service, PageFrom request) {
        Object[] arguments = Arrays.stream(entry.getParameterTypes()).map(type -> {
            if (PageFrom.class.isAssignableFrom(type)) {
                return request;
            }
            return type == Authentication.class
                    ? UsernamePasswordAuthenticationToken.authenticated("synthetic-sort-viewer", null, List.of())
                    : null;
        }).toArray();
        return assertThrows(InvocationTargetException.class, () -> entry.invoke(service, arguments)).getCause();
    }

    static Stream<Target> sqlTargets() {
        return targets().filter(target -> !target.fields().isEmpty());
    }

    static Stream<Target> targets() {
        return Stream.of(
                new Target(com.devops00.spectra.oa.calendar.service.impl.CalendarServiceImpl.class, "page",
                        Map.of("id", "id", "start_time", "start_time", "end_time", "end_time")),
                new Target(com.devops00.spectra.oa.asset.service.impl.AssetServiceImpl.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.oa.application.service.impl.ApplicationServiceImpl.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.oa.supply.service.impl.SupplyServiceImpl.class, "page",
                        Map.of("id", "id", "name", "name", "sku", "sku")),
                new Target(com.devops00.spectra.oa.leave.service.impl.LeaveServiceImpl.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.oa.document.service.impl.DocumentServiceImpl.class, "page",
                        Map.of("id", "id", "updated_at", "updated_at")),
                new Target(com.devops00.spectra.core.user.service.impl.UserServiceImpl.class, "page",
                        Map.of("id", "id", "username", "username", "real_name", "real_name", "employee_no", "employee_no", "created_at",
                                "created_at")),
                new Target(com.devops00.spectra.oa.contract.service.impl.ContractServiceImpl.class, "page",
                        Map.of("id", "id", "updated_at", "updated_at")),
                new Target(com.devops00.spectra.oa.reimbursement.service.impl.ReimbursementServiceImpl.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.oa.purchase.service.impl.PurchaseServiceImpl.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.oa.notice.service.impl.NoticeServiceImpl.class, "page",
                        Map.of("id", "id", "publish_at", "publish_at", "created_at", "created_at")),
                new Target(com.devops00.spectra.core.user.service.impl.DepartmentMemberQueryServiceImpl.class, "page",
                        Map.of("id", "user_record.id", "username", "user_record.username", "real_name", "user_record.real_name", "status",
                                "user_record.status")),
                new Target(com.devops00.spectra.core.notification.service.impl.NotificationInboxServiceImpl.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.core.notification.service.impl.NotificationAdminServiceImpl.class, "pageRequests",
                        Map.of("id", "id", "created_at", "created_at", "status", "status")),
                new Target(com.devops00.spectra.core.notification.service.impl.NotificationAdminServiceImpl.class, "pageTasks",
                        Map.of("id", "id", "created_at", "created_at", "status", "status", "channel", "channel")),
                new Target(com.devops00.spectra.core.notification.service.impl.NotificationAdminServiceImpl.class, "pageDeliveries",
                        Map.of("id", "d.id", "created_at", "d.created_at", "result_status", "d.result_status", "channel", "t.channel")),
                new Target(com.devops00.spectra.oa.meeting.service.impl.MeetingServiceImpl.class, "page",
                        Map.of("id", "id", "start_time", "start_time")),
                new Target(com.devops00.spectra.core.system.service.impl.RegionServiceImpl.class, "page",
                        Map.of("id", "id", "code", "code", "name", "name", "sort", "sort")),
                new Target(com.devops00.spectra.core.upload.controller.FileUploadAdminController.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.core.upload.controller.FileTypeController.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.core.upload.controller.FileReferenceController.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.core.upload.controller.FileAssetController.class, "page",
                        Map.of("id", "id", "created_at", "created_at")),
                new Target(com.devops00.spectra.workflow.service.impl.TaskServiceImpl.class, "todo",
                        Map.of()),
                new Target(com.devops00.spectra.workflow.service.impl.TaskServiceImpl.class, "done",
                        Map.of()),
                new Target(com.devops00.spectra.oa.contact.service.impl.ContactServiceImpl.class, "page",
                        Map.of()),
                new Target(com.devops00.spectra.core.user.service.impl.RoleServiceImpl.class, "page",
                        Map.of()),
                new Target(com.devops00.spectra.core.user.service.impl.UserServiceImpl.class, "online",
                        Map.of()),
                new Target(com.devops00.spectra.core.notification.service.impl.NotificationTemplateServiceImpl.class, "page",
                        Map.of()),
                new Target(com.devops00.spectra.core.notification.service.impl.NotificationTemplateServiceImpl.class, "groupPage",
                        Map.of()),
                new Target(com.devops00.spectra.core.audit.service.AuditLogQueryService.class, "page",
                        Map.of()),
                new Target(com.devops00.spectra.core.quartz.service.impl.QuartzJobManagementServiceImpl.class, "jobs",
                        Map.of()),
                new Target(com.devops00.spectra.core.quartz.service.impl.QuartzJobManagementServiceImpl.class, "executionHistory",
                        Map.of()));
    }

    record Target(Class<?> type, String method, Map<String, String> fields) {
        Method entry() {
            return Arrays.stream(type.getMethods())
                    .filter(candidate -> candidate.getName().equals(method))
                    .filter(candidate -> Arrays.stream(candidate.getParameterTypes()).anyMatch(PageFrom.class::isAssignableFrom))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public String toString() {
            return type.getSimpleName() + "." + method;
        }
    }

    private static final class ConvertedPage extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final Page<?> page;

        private ConvertedPage(Page<?> page) {
            this.page = page;
        }
    }
}
