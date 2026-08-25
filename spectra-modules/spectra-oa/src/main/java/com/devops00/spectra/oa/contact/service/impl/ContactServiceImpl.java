/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.oa.contact.javabean.converter.ContactConverter;
import com.devops00.spectra.oa.contact.javabean.vo.ContactVO;
import com.devops00.spectra.oa.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
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

    private static final UserStatus ENABLED = UserStatus.ACTIVE;

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final ContactConverter contactConverter;
    private final UserContactService userContactService;

    @Override
    public IPage<ContactVO> page(PageFrom page, String keyword) {
        var wrapper = new LambdaQueryWrapper<User>().eq(User::getStatus, ENABLED);
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(query -> query.like(User::getEmployeeNo, value)
                    .or()
                    .like(User::getRealName, value)
                    .or()
                    .like(User::getUsername, value));
        }
        wrapper.orderByAsc(User::getRealName).orderByAsc(User::getEmployeeNo);
        var users = userMapper.selectPage(page.toPage(), wrapper);
        var departmentIds = users.getRecords().stream().map(User::getDepartmentId).filter(Objects::nonNull).distinct().toList();
        Map<UUID, Department> departments = departmentIds.isEmpty()
                ? Collections.emptyMap()
                : departmentMapper.selectByIds(departmentIds).stream().collect(Collectors.toMap(Department::getId, Function.identity()));
        var contacts = userContactService.listActiveByUserIds(users.getRecords().stream().map(User::getId).toList());
        var result = new Page<ContactVO>(users.getCurrent(), users.getSize(), users.getTotal());
        result.setRecords(users.getRecords().stream().map(user -> {
            var vo = contactConverter.toVO(user);
            vo.setUsername(user.getUsername());
            var userContacts = contacts.getOrDefault(user.getId(), java.util.List.of());
            vo.setPhone(contactValue(userContacts, UserContactService.PHONE));
            vo.setEmail(contactValue(userContacts, UserContactService.EMAIL));
            var department = user.getDepartmentId() == null ? null : departments.get(user.getDepartmentId());
            vo.setDepartmentName(department == null ? null : StringUtils.hasText(department.getPath()) ? department.getPath() : department.getName());
            return vo;
        }).toList());
        return result;
    }

    private String contactValue(java.util.List<UserContact> contacts, String type) {
        return contacts.stream()
                .filter(contact -> type.equals(contact.getContactType()))
                .map(UserContact::getContactValue)
                .findFirst()
                .orElse(null);
    }
}
