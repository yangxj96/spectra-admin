/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.common.enums.AccountType;
import com.yangxj96.spectra.service.auth.javabean.entity.Account;
import com.yangxj96.spectra.service.auth.javabean.entity.Role;
import com.yangxj96.spectra.service.auth.javabean.entity.User;
import com.yangxj96.spectra.service.auth.javabean.from.UserPageFrom;
import com.yangxj96.spectra.service.auth.javabean.from.UserSaveFrom;
import com.yangxj96.spectra.service.auth.javabean.mapstruct.PermissionMapstruct;
import com.yangxj96.spectra.service.auth.javabean.mapstruct.UserMapstruct;
import com.yangxj96.spectra.service.auth.javabean.vo.UserPageVO;
import com.yangxj96.spectra.service.auth.mapper.UserMapper;
import com.yangxj96.spectra.service.auth.service.AccountService;
import com.yangxj96.spectra.service.auth.service.RoleService;
import com.yangxj96.spectra.service.auth.service.UserService;
import com.yangxj96.spectra.share.javabean.ShareOrganizationDTO;
import com.yangxj96.spectra.share.service.ShareOrganizationService;
import com.yangxj96.spectra.starter.common.exception.DataNotExistException;
import com.yangxj96.spectra.starter.secruity.properties.UserProperties;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Lazy
    @Resource
    private UserService self;

    @Resource
    private UserMapstruct mapstruct;

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionMapstruct permissionMapstruct;

    @Resource
    private AccountService accountService;

    @Resource
    private UserProperties userProperties;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private ObjectProvider<ShareOrganizationService> shareOrganizationServices;


    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        var result = new Page<UserPageVO>();
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName());
        Page<User> db = this.page(page.toPage(), wrapper);
        BeanUtils.copyProperties(db, result);
        result.setRecords(mapstruct.toVOs(db.getRecords()));

        // 获取所需内容
        Map<Long, String> organizationNameMap = Optional.ofNullable(shareOrganizationServices.getIfAvailable())
                .map(ShareOrganizationService::all)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(ShareOrganizationDTO::getId, ShareOrganizationDTO::getName));

        // vo扩展字段补充
        result.getRecords().forEach(vo -> {
            var roles = roleService.getByUserId(vo.getId());
            if (null != roles && !roles.isEmpty()) {
                vo.setRoles(permissionMapstruct.roleToVOs(roles));
            }
            vo.setOrganizationName(organizationNameMap.getOrDefault(vo.getOrganizationId(), null));
        });
        // 响应
        return result;
    }

    @Override
    @Transactional
    public void create(UserSaveFrom params) {
        User entity = mapstruct.toEntity(params);
        if (!this.save(entity)) {
            throw new RuntimeException("保存用户信息异常");
        }
        // 构建默认的账号信息
        Account account = Account.builder()
                .username(entity.getEmail())
                .password(passwordEncoder.encode(userProperties.getDefaultPassword()))
                .userId(entity.getId())
                .type(AccountType.DEFAULT)
                .build();
        if (!accountService.save(account)) {
            throw new RuntimeException("保存账号信息异常");
        }
        // 关联角色
        roleService.insertRelevanceRoles(entity.getId(), params.getRoleIds());
    }

    @Override
    @Transactional
    public void updateById(UserSaveFrom params) {
        User user = this.getById(params.getId());
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        User entity = mapstruct.toEntity(params);
        if (!self.updateById(entity)) {
            throw new RuntimeException("更新用户发生错误");
        }
        // 如果邮箱有过修改,则应该同时修改账号
        if (!entity.getEmail().equals(user.getEmail())) {
            var account = accountService.getDefaultAccountByUserId(entity.getId());
            account.setUsername(entity.getEmail());
            accountService.updateById(account);
        }
        // 判断角色是否修改过
        List<Long> roleIds = roleService.getByUserId(entity.getId()).stream().map(Role::getId).sorted().toList();
        Collections.sort(params.getRoleIds());
        // 角色列表变化了
        if (!roleIds.equals(params.getRoleIds())) {
            if (roleService.removeRelevanceRoles(entity.getId()) < 0) {
                throw new RuntimeException("移除过期的角色关联错误");
            }
            if (roleService.insertRelevanceRoles(entity.getId(), params.getRoleIds()) <= 0) {
                throw new RuntimeException("新增角色列表错误");
            }
        }
    }

    @Override
    @Transactional
    public void deleteById(String uid) {
        var user = this.getById(uid);
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        // 强制注销账号登录信息
        accountService.getByUserId(user.getId()).forEach(account -> StpUtil.logout(account.getId()));
        // 先删除角色关联
        roleService.removeRelevanceRoles(user.getId());
        // 删除相关账号信息
        accountService.removeByUserId(user.getId());
        // 删除用户信息
        this.removeById(user);
    }
}
