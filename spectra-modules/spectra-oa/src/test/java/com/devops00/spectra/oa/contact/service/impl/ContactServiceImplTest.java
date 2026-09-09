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

package com.devops00.spectra.oa.contact.service.impl;

import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.common.port.directory.DirectoryContactSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.common.port.directory.DirectoryUserSnapshot;
import com.devops00.spectra.oa.contact.javabean.converter.ContactConverter;
import com.devops00.spectra.oa.contact.javabean.vo.ContactVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 通讯录通过 Directory Port 查询的行为测试。 */
class ContactServiceImplTest {

    @Test
    void returnsPagedActiveUsersWithDepartmentAndContactSnapshots() {
        var directory = mock(DirectoryQueryPort.class);
        var converter = mock(ContactConverter.class);
        var departmentId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var user = new DirectoryUserSnapshot(userId, "E001", "Alice", "alice", "avatar.png", "ACTIVE", departmentId);
        var department = new DirectoryDepartmentSnapshot(departmentId, null, "研发部", "总部/研发部");
        var contact = new DirectoryContactSnapshot("PHONE", "13800000000");
        var vo = new ContactVO();
        vo.setId(userId);
        when(directory.listUsers()).thenReturn(List.of(user));
        when(directory.findDepartmentsByIds(List.of(departmentId))).thenReturn(List.of(department));
        when(directory.findActiveContactsByUserIds(List.of(userId))).thenReturn(Map.of(userId, List.of(contact)));
        when(converter.toVO(user)).thenReturn(vo);

        var service = new ContactServiceImpl(directory, converter);
        var page = new PageFrom(10L, 1L, null);

        var result = service.page(page, "Alice");

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(userId);
            assertThat(item.getUsername()).isEqualTo("alice");
            assertThat(item.getPhone()).isEqualTo("13800000000");
            assertThat(item.getEmail()).isNull();
            assertThat(item.getDepartmentName()).isEqualTo("总部/研发部");
        });
        verify(directory).listUsers();
        verify(directory).findDepartmentsByIds(List.of(departmentId));
        verify(directory).findActiveContactsByUserIds(List.of(userId));
    }
}
