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

package com.devops00.spectra.oa.notification;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OA 通知调用方迁移契约测试，防止业务服务绕过快捷通知服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class NotificationCallerMigrationContractTest {

    private static final List<String> CALLERS = List.of(
            "contract/service/impl/ContractServiceImpl.java",
            "document/service/impl/DocumentServiceImpl.java",
            "leave/service/impl/LeaveServiceImpl.java",
            "meeting/service/impl/MeetingServiceImpl.java",
            "notice/service/impl/NoticeServiceImpl.java",
            "purchase/service/impl/PurchaseServiceImpl.java",
            "reimbursement/service/impl/ReimbursementServiceImpl.java");

    @Test
    void shouldKeepEveryOaNotificationCallerOnShortcutServiceWithStableKey() throws Exception {
        for (var caller : CALLERS) {
            var source = readSource(caller);
            if (source.contains("OaApplicationWorkflowSupport")) {
                assertTrue(source.contains("workflowSupport.sendNotification("), caller);
                assertFalse(source.contains("com.devops00.spectra.core.notification"), caller);
                continue;
            }
            assertTrue(source.contains("import com.devops00.spectra.common.notification.NotificationService;"), caller);
            assertTrue(source.contains("NotificationSendRequest.inApp("), caller);
            assertTrue(source.contains("NotificationTemplateCode."), caller);
            assertTrue(source.contains("\"oa:"), caller);
            assertFalse(source.contains("com.devops00.spectra.core.notification"), caller);
        }
        var support = readSource("application/support/OaApplicationWorkflowSupport.java");
        assertTrue(support.contains("NotificationSendRequest.inApp("));
        assertTrue(support.contains("\"oa:"));
        assertTrue(support.contains("businessReference("));
    }

    private String readSource(String relativePath) throws Exception {
        var candidates = List.of(
                Path.of("src/main/java/com/devops00/spectra/oa", relativePath),
                Path.of("spectra-oa/src/main/java/com/devops00/spectra/oa", relativePath),
                Path.of("..", "src/main/java/com/devops00/spectra/oa", relativePath));
        var file = candidates.stream()
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到 OA 通知调用方源码: " + relativePath));
        return Files.readString(file);
    }
}
