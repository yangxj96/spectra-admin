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

package com.yangxj96.spectra.core.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.common.base.BaseEntity;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.common.enums.AccountType;
import com.yangxj96.spectra.common.exception.DataNotExistException;
import com.yangxj96.spectra.core.auth.javabean.entity.Account;
import com.yangxj96.spectra.core.auth.service.AccountService;
import com.yangxj96.spectra.core.system.javabean.entity.Organization;
import com.yangxj96.spectra.core.system.service.OrganizationService;
import com.yangxj96.spectra.core.user.javabean.entity.Role;
import com.yangxj96.spectra.core.user.javabean.entity.User;
import com.yangxj96.spectra.core.user.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.user.javabean.from.UserSaveFrom;
import com.yangxj96.spectra.core.user.javabean.mapstruct.PermissionMapstruct;
import com.yangxj96.spectra.core.user.javabean.mapstruct.UserMapstruct;
import com.yangxj96.spectra.core.user.javabean.vo.UserPageVO;
import com.yangxj96.spectra.core.user.mapper.UserMapper;
import com.yangxj96.spectra.core.user.service.RoleService;
import com.yangxj96.spectra.core.user.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    private AccountService accountServices;

    @Resource
    private OrganizationService organizationService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${spectra.user.default-password}")
    private String defaultPassword;

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        var result = new Page<UserPageVO>();
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName())
                .like(StringUtils.isNotBlank(params.getEmail()), User::getEmail, params.getEmail())
                .ne(BaseEntity::getId, StpUtil.getTerminalInfo().getExtra("user_id"))
                .eq(params.getStatus() != null, User::getState, params.getStatus());

        var db = this.page(page.toPage(), wrapper);
        BeanUtils.copyProperties(db, result);
        result.setRecords(mapstruct.toVOs(db.getRecords()));

        // 获取所需内容
        var organizationNameMap = organizationService.list()
                .stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getName));

        // vo扩展字段补充
        result.getRecords().forEach(vo -> {
            var roles = roleService.getByUserId(vo.getId());
            if (null != roles && !roles.isEmpty()) {
                vo.setRoles(permissionMapstruct.roleToVOs(roles));
            }
            vo.setOrganizationName(organizationNameMap.getOrDefault(vo.getOrganizationId(), ""));
        });
        // 响应
        return result;
    }

    @Override
    @Transactional
    public void create(UserSaveFrom params) {
        var entity = mapstruct.toEntity(params);
        if (!this.save(entity)) {
            throw new RuntimeException("保存用户信息异常");
        }
        // 构建默认的账号信息
        var account = Account.builder()
                .username(entity.getEmail())
                .password(passwordEncoder.encode(defaultPassword))
                .userId(entity.getId())
                .type(AccountType.DEFAULT)
                .build();
        if (!accountServices.save(account)) {
            throw new RuntimeException("保存账号信息异常");
        }
        // 关联角色
        roleService.insertRelevanceRoles(entity.getId(), params.getRoleIds());
    }

    @Override
    @Transactional
    public void updateById(UserSaveFrom params) {
        var user = this.getById(params.getId());
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        var entity = mapstruct.toEntity(params);
        if (!self.updateById(entity)) {
            throw new RuntimeException("更新用户发生错误");
        }
        // 如果邮箱有过修改,则应该同时修改账号
        if (!entity.getEmail().equals(user.getEmail())) {
            var account = accountServices.getDefaultAccountByUserId(entity.getId());
            account.setUsername(entity.getEmail());
            accountServices.updateById(account);
        }
        // 判断角色是否修改过,有角色就要判断下角色是否修改过了
        if (CollectionUtils.isNotEmpty(params.getRoleIds())) {
            var roleIds = roleService.getByUserId(entity.getId())
                    .stream()
                    .map(Role::getId)
                    .sorted()
                    .toList();
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
    }

    @Override
    @Transactional
    public void deleteById(String uid) {
        var user = this.getById(uid);
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        // 强制注销账号登录信息
        accountServices.getByUserId(user.getId()).forEach(account -> StpUtil.logout(account.getId()));
        // 先删除角色关联
        roleService.removeRelevanceRoles(user.getId());
        // 删除相关账号信息
        accountServices.removeByUserId(user.getId());
        // 删除用户信息
        this.removeById(user);
    }
}
