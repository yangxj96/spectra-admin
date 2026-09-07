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

package com.devops00.spectra.common.port.directory;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryPortContractTest {

    @Test
    void directoryPortMustExposeOnlyStableSnapshotQueries() throws NoSuchMethodException {
        assertThat(DirectoryQueryPort.class.isInterface()).isTrue();
        assertThat(Arrays.stream(DirectoryQueryPort.class.getDeclaredMethods())
                .map(Method::getName)
                .toList())
                .containsExactlyInAnyOrder(
                        "listUsers",
                        "findUsersByDepartmentId",
                        "findUsersByIds",
                        "findDepartmentsByIds",
                        "listDepartments",
                        "findActiveContactsByUserIds");
        assertThat(DirectoryQueryPort.class.getMethod("listUsers").getReturnType())
                .isEqualTo(List.class);
        assertThat(DirectoryQueryPort.class.getMethod("findUsersByDepartmentId", UUID.class).getReturnType())
                .isEqualTo(List.class);
        assertThat(DirectoryQueryPort.class.getMethod("findUsersByIds", Collection.class).getReturnType())
                .isEqualTo(List.class);
        assertThat(DirectoryQueryPort.class.getMethod("findDepartmentsByIds", Collection.class).getReturnType())
                .isEqualTo(List.class);
        assertThat(DirectoryQueryPort.class.getMethod("listDepartments").getReturnType())
                .isEqualTo(List.class);
        assertThat(DirectoryQueryPort.class.getMethod("findActiveContactsByUserIds", Collection.class)
                .getReturnType())
                .isEqualTo(Map.class);
    }

    @Test
    void directorySnapshotsMustBeImmutableRecords() {
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        var user = new DirectoryUserSnapshot(userId, "E001", "Alice", "alice", "avatar.png", "ACTIVE", departmentId);
        var department = new DirectoryDepartmentSnapshot(departmentId, null, "Engineering", "HQ/Engineering");
        var contact = new DirectoryContactSnapshot("EMAIL", "alice@example.com");

        assertThat(DirectoryUserSnapshot.class.isRecord()).isTrue();
        assertThat(DirectoryDepartmentSnapshot.class.isRecord()).isTrue();
        assertThat(DirectoryContactSnapshot.class.isRecord()).isTrue();
        assertThat(user.id()).isEqualTo(userId);
        assertThat(user.displayName()).isEqualTo("Alice");
        assertThat(user.status()).isEqualTo("ACTIVE");
        assertThat(user.departmentId()).isEqualTo(departmentId);
        assertThat(department.parentId()).isNull();
        assertThat(department.path()).isEqualTo("HQ/Engineering");
        assertThat(contact.contactType()).isEqualTo("EMAIL");
        assertThat(contact.contactValue()).isEqualTo("alice@example.com");
        assertThat(Arrays.stream(DirectoryUserSnapshot.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().startsWith("set"))).isTrue();
    }
}
