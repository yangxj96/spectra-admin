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

package com.devops00.spectra.core.directory;

import com.devops00.spectra.common.port.directory.DirectoryContactSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryUserSnapshot;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectoryQueryAdapterTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private UserContactService userContactService;

    @Test
    void emptyInputsReturnEmptyResultsWithoutQueryingCoreDependencies() {
        var adapter = adapter();

        assertThat(adapter.findUsersByIds(List.of())).isEmpty();
        assertThat(adapter.findDepartmentsByIds(List.of())).isEmpty();
        assertThat(adapter.findActiveContactsByUserIds(List.of())).isEmpty();

        verifyNoInteractions(userMapper, departmentMapper, userContactService);
    }

    @Test
    void usersAreDeduplicatedBatchLoadedAndMappedToImmutableSnapshots() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        var first = new User();
        first.setId(firstId);
        first.setEmployeeNo("E001");
        first.setRealName("Alice");
        first.setUsername("alice");
        first.setStatus(UserStatus.ACTIVE);
        first.setDepartmentId(secondId);
        var second = new User();
        second.setId(secondId);
        second.setEmployeeNo("E002");
        second.setRealName("Bob");
        second.setUsername("bob");
        second.setStatus(UserStatus.LOCKED);
        when(userMapper.selectByIds(List.of(firstId, secondId))).thenReturn(List.of(first, second));

        var result = adapter().findUsersByIds(Arrays.asList(firstId, firstId, null, secondId));

        assertThat(result).extracting(DirectoryUserSnapshot::id).containsExactly(firstId, secondId);
        assertThat(result.get(0).displayName()).isEqualTo("Alice");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        assertThatThrownBy(() -> result.add(null)).isInstanceOf(UnsupportedOperationException.class);
        verify(userMapper).selectByIds(List.of(firstId, secondId));
    }

    @Test
    void departmentsAreBatchLoadedAndListQueriesMapToImmutableSnapshots() {
        UUID departmentId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        var department = new Department();
        department.setId(departmentId);
        department.setPid(parentId);
        department.setName("Engineering");
        department.setPath("HQ/Engineering");
        when(departmentMapper.selectByIds(List.of(departmentId))).thenReturn(List.of(department));
        when(departmentMapper.selectList(isNull())).thenReturn(List.of(department));

        var adapter = adapter();
        var selected = adapter.findDepartmentsByIds(List.of(departmentId, departmentId));
        var listed = adapter.listDepartments();

        assertThat(selected).containsExactly(new DirectoryDepartmentSnapshot(
                departmentId, parentId, "Engineering", "HQ/Engineering"));
        assertThat(listed).containsExactly(new DirectoryDepartmentSnapshot(
                departmentId, parentId, "Engineering", "HQ/Engineering"));
        assertThatThrownBy(() -> listed.add(null)).isInstanceOf(UnsupportedOperationException.class);
        verify(departmentMapper).selectByIds(List.of(departmentId));
        verify(departmentMapper).selectList(isNull());
    }

    @Test
    void activeContactsAreGroupedByUserAndNestedResultsAreImmutable() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        var phone = new UserContact();
        phone.setUserId(firstId);
        phone.setContactType(UserContactService.PHONE);
        phone.setContactValue("13800000000");
        var email = new UserContact();
        email.setUserId(firstId);
        email.setContactType(UserContactService.EMAIL);
        email.setContactValue("alice@example.com");
        Map<UUID, List<UserContact>> source = new LinkedHashMap<>();
        source.put(firstId, List.of(phone, email));
        when(userContactService.listActiveByUserIds(List.of(firstId, secondId))).thenReturn(source);

        var result = adapter().findActiveContactsByUserIds(List.of(firstId, firstId, secondId));

        assertThat(result.get(firstId)).containsExactly(
                new DirectoryContactSnapshot("PHONE", "13800000000"),
                new DirectoryContactSnapshot("EMAIL", "alice@example.com"));
        assertThat(result).doesNotContainKey(secondId);
        assertThatThrownBy(() -> result.put(secondId, List.of()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.get(firstId).add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        verify(userContactService).listActiveByUserIds(List.of(firstId, secondId));
    }

    private DirectoryQueryAdapter adapter() {
        return new DirectoryQueryAdapter(userMapper, departmentMapper, userContactService);
    }
}
