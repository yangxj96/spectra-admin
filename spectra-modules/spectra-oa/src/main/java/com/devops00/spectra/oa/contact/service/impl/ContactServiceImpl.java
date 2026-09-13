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

package com.devops00.spectra.oa.contact.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.common.port.directory.DirectoryContactSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.common.port.directory.DirectoryUserSnapshot;
import com.devops00.spectra.oa.contact.javabean.converter.ContactConverter;
import com.devops00.spectra.oa.contact.javabean.vo.ContactVO;
import com.devops00.spectra.oa.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 通讯录直接复用系统用户和部门数据，不维护重复的 OA 联系人主表。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private static final String ENABLED = "ACTIVE";
    private static final String PHONE = "PHONE";
    private static final String EMAIL = "EMAIL";

    private final DirectoryQueryPort directoryQueryPort;
    private final ContactConverter contactConverter;

    @Override
    public IPage<ContactVO> page(PageFrom page, String keyword) {
        var users = directoryQueryPort.listUsers()
                .stream()
                .filter(user -> ENABLED.equals(user.status()))
                .filter(user -> matches(user, keyword))
                .sorted(Comparator.comparing(DirectoryUserSnapshot::displayName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(DirectoryUserSnapshot::employeeNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        var total = users.size();
        int fromIndex = Math.toIntExact(Math.min(pageOffset(page), total));
        var toIndex = Math.min(fromIndex + pageSize(page), total);
        var currentUsers = users.subList(fromIndex, toIndex);
        var departmentIds = currentUsers.stream().map(DirectoryUserSnapshot::departmentId).filter(Objects::nonNull).distinct().toList();
        Map<UUID, DirectoryDepartmentSnapshot> departments = directoryQueryPort.findDepartmentsByIds(departmentIds)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DirectoryDepartmentSnapshot::id, value -> value, (left, right) -> left));
        var contacts = directoryQueryPort.findActiveContactsByUserIds(currentUsers.stream().map(DirectoryUserSnapshot::id).toList());
        var result = new Page<ContactVO>(page.getPageNum(), page.getPageSize(), total);
        result.setRecords(currentUsers.stream().map(user -> {
            var vo = contactConverter.toVO(user);
            vo.setUsername(user.username());
            var userContacts = contacts.getOrDefault(user.id(), List.of());
            vo.setPhone(contactValue(userContacts, PHONE));
            vo.setEmail(contactValue(userContacts, EMAIL));
            var department = user.departmentId() == null ? null : departments.get(user.departmentId());
            vo.setDepartmentName(department == null ? null : StringUtils.hasText(department.path()) ? department.path() : department.name());
            return vo;
        }).toList());
        return result;
    }

    /**
     * 处理相关数据相关数据。
     */
    private static boolean matches(DirectoryUserSnapshot user, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        var value = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(user.employeeNo(), value) || contains(user.displayName(), value) || contains(user.username(), value);
    }

    /**
     * 判断相关数据。
     */
    private static boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    /**
     * 按查询条件分页查询相关数据。
     */
    private static long pageOffset(PageFrom page) {
        return Math.max(0L, (page.getPageNum() - 1L) * page.getPageSize());
    }

    /**
     * 按查询条件分页查询相关数据。
     */
    private static int pageSize(PageFrom page) {
        return Math.toIntExact(Math.max(0L, page.getPageSize()));
    }

    /**
     * 处理值相关数据。
     */
    private String contactValue(List<DirectoryContactSnapshot> contacts, String type) {
        return contacts.stream()
                .filter(contact -> type.equals(contact.contactType()))
                .map(DirectoryContactSnapshot::contactValue)
                .findFirst()
                .orElse(null);
    }
}
