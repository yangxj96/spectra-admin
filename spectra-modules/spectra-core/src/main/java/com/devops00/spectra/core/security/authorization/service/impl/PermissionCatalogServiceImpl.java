/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.service.PermissionCatalogService;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Permission Catalog 只读适配器。
 * <p>
 * 资源分组节点只服务于 UX 展示，不参与 RolePermission 语义，也不会被写入目标数据库。
 */
@Service
@RequiredArgsConstructor
public class PermissionCatalogServiceImpl implements PermissionCatalogService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<AuthorityTreeVO> tree() {
        var permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getState, "ACTIVE")
                .orderByAsc(Permission::getResourceCode)
                .orderByAsc(Permission::getActionCode)
                .orderByAsc(Permission::getCode))
                .stream()
                .filter(permission -> "ACTIVE".equals(permission.getState()))
                .sorted(Comparator.comparing(this::resourceCode)
                        .thenComparing(permission -> valueOrEmpty(permission.getActionCode()))
                        .thenComparing(permission -> valueOrEmpty(permission.getCode())))
                .toList();
        var groups = new LinkedHashMap<String, AuthorityTreeVO>();
        for (var permission : permissions) {
            var resourceCode = resourceCode(permission);
            var group = groups.computeIfAbsent(resourceCode, this::resourceNode);
            if (group.getChildren() == null) {
                group.setChildren(new ArrayList<>());
            }
            var leaf = new AuthorityTreeVO();
            leaf.setId(permission.getId());
            leaf.setPid(group.getId());
            leaf.setName(permission.getName());
            leaf.setCode(permission.getCode());
            leaf.setAllowedScopeModes(parseScopeModes(permission.getAllowedScopeModes()));
            leaf.setSort(0);
            group.getChildren().add(leaf);
        }
        return new ArrayList<>(groups.values());
    }

    private AuthorityTreeVO resourceNode(String resourceCode) {
        var node = new AuthorityTreeVO();
        node.setId(UUID.nameUUIDFromBytes(("permission-resource:" + resourceCode).getBytes(StandardCharsets.UTF_8)));
        node.setName(resourceCode);
        node.setCode(resourceCode);
        node.setSort(0);
        node.setChildren(new ArrayList<>());
        return node;
    }

    private String resourceCode(Permission permission) {
        if (permission.getResourceCode() != null && !permission.getResourceCode().isBlank()) {
            return permission.getResourceCode();
        }
        var code = permission.getCode();
        var separator = code == null ? -1 : code.indexOf(':');
        return separator > 0 ? code.substring(0, separator) : "uncategorized";
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private List<String> parseScopeModes(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(mode -> !mode.isBlank())
                .map(mode -> mode.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
