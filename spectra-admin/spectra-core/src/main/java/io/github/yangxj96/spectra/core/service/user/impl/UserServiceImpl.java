/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.exception.DataSaveException;
import io.github.yangxj96.spectra.common.exception.EntityUpdateException;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.configure.security.enums.LoginType;
import io.github.yangxj96.spectra.core.configure.system.UserProperties;
import io.github.yangxj96.spectra.core.javabean.auth.entity.Account;
import io.github.yangxj96.spectra.core.javabean.system.entity.Organization;
import io.github.yangxj96.spectra.core.javabean.user.converter.RoleConverter;
import io.github.yangxj96.spectra.core.javabean.user.converter.UserConverter;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.javabean.user.from.UserPageFrom;
import io.github.yangxj96.spectra.core.javabean.user.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserOnlineVO;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserPageVO;
import io.github.yangxj96.spectra.core.mapper.user.UserMapper;
import io.github.yangxj96.spectra.core.service.auth.AccountService;
import io.github.yangxj96.spectra.core.service.system.OrganizationService;
import io.github.yangxj96.spectra.core.service.user.RelUserRoleService;
import io.github.yangxj96.spectra.core.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Service
@EnableConfigurationProperties({UserProperties.class})
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserConverter userConverter;

    @Resource
    private RoleConverter roleConverter;

    @Resource
    private RelUserRoleService relUserRoleService;

    @Resource
    private OrganizationService organizationService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    private UserProperties userProperties;

    @Resource
    private AccountService accountService;

    @Override
    @Transactional
    public void create(UserSaveFrom params) {
        var entity = userConverter.toEntity(params);
        if (StrUtils.isBlank(entity.getUsername())) {
            entity.setUsername(IdWorker.get32UUID().substring(0, 6));
        }
        if (!this.save(entity)) {
            throw new DataSaveException("保存用户信息异常");
        }
        // 创建一个默认的账号密码登录
        var defaultAccount = Account
                .builder()
                .userId(entity.getId())
                .type(LoginType.PASSWORD)
                .loginName(entity.getEmail())
                .password(passwordEncoder.encode(userProperties.getDefaultPassword()))
                .provider("DEFAULT")
                .status(Boolean.TRUE)
                .build();
        if (!accountService.save(defaultAccount)) {
            throw new DataSaveException("保存用户信息异常");
        }
        // 关联角色
        relUserRoleService.grant(entity.getId(), params.getRoleIds());
    }

    @Override
    @Transactional
    public void deleteById(String uid) {
        var user = this.getById(uid);
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        // TODO 根据用户强制注销账号登录信息
        //SecUtil.kick(user.getId());
        // 先删除角色关联
        relUserRoleService.revoke(user.getId());
        // 删除账号信息
        accountService.deleteByUserId(user.getId());
        // 删除用户信息
        this.removeById(user);
    }

    @Override
    @Transactional
    public void updateById(UserSaveFrom params) {
        var entity = this.getById(params.getId());
        if (null == entity) {
            throw new DataNotExistException("用户不存在");
        }
        // 默认账号是否需要修改
        boolean defaultAccountUpdateFlag = params.getEmail().equals(entity.getEmail());
        userConverter.updateUserFrom(params, entity);
        if (this.baseMapper.updateById(entity) == 0) {
            throw new EntityUpdateException("更新用户发生错误");
        }

        // 默认账号处理
        if (!defaultAccountUpdateFlag) {
            Account account = accountService.getDefaultByUserId(entity.getId());
            account.setLoginName(entity.getEmail());
            accountService.updateById(account);
        }

        // 角色处理
        // 判断角色是否修改过,有角色就要判断下角色是否修改过了
        var currentRoles = new HashSet<>(relUserRoleService.getRoles(params.getId()).stream().map(Role::getId).toList());
        var targetRoles = new HashSet<>(params.getRoleIds() != null ? params.getRoleIds() : List.of());

        // 计算要删除的
        var roleToDelete = new HashSet<>(currentRoles);
        roleToDelete.removeAll(targetRoles);

        if (!roleToDelete.isEmpty()) {
            List<Long> deleteList = List.copyOf(roleToDelete);
            try {
                relUserRoleService.revoke(entity.getId(), deleteList);
            } catch (Exception e) {
                log.error("删除角色关联失败，未完全删除,{}", e.getMessage(), e);
                throw new EntityUpdateException("删除角色关联失败，未完全删除");
            }
        }

        // 计算要插入的角色
        var roleToInsert = new HashSet<>(targetRoles);
        roleToInsert.removeAll(currentRoles);

        if (!roleToInsert.isEmpty()) {
            List<Long> insertList = List.copyOf(roleToInsert);
            try {
                relUserRoleService.grant(entity.getId(), insertList);
            } catch (Exception e) {
                log.error("新增角色关联失败，未完全插入,{}", e.getMessage(), e);
                throw new EntityUpdateException("新增角色关联失败，未完全插入");
            }
        }
    }

    @Override
    @Transactional
    public void passwordResetById(String uid) {
        try {
            var user = this.getById(Long.parseLong(uid));
            Account account = accountService.getDefaultByUserId(user.getId());
            account.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
            accountService.updateById(account);
        } catch (Exception e) {
            log.error("用户不存在", e);
            throw new DataNotExistException("用户不存在");
        }
    }

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        var result = new Page<UserPageVO>();
        List<Long> organizationIds = new ArrayList<>();
        if (params.getOrganizationId() != null) {
            Organization organization = organizationService.getById(params.getOrganizationId());
            List<Organization> listed = organizationService.list(
                    new LambdaQueryWrapper<Organization>()
                            .eq(Organization::getId, organization.getId())
                            .or()
                            .likeRight(Organization::getPath, organization.getPath())
            );
            organizationIds = listed.stream().map(BaseEntity::getId).toList();
            log.info("organizationIds:{}", organizationIds);
        }

        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StrUtils.isNotBlank(params.getUsername()), User::getUsername, params.getUsername())
                .like(StrUtils.isNotBlank(params.getEmail()), User::getEmail, params.getEmail())
                .in(CollUtils.isNotEmpty(organizationIds), User::getOrganizationId, organizationIds)
                .eq(params.getStatus() != null, User::getStatus, params.getStatus());

        var db = this.page(page.toPage(), wrapper);
        BeanUtils.copyProperties(db, result);
        result.setRecords(userConverter.toVOs(db.getRecords()));

        // 获取所需内容
        var organizationNameMap = organizationService.list()
                .stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getPath));

        // 扩展字段补充
        result.getRecords().forEach(vo -> {
            var roles = relUserRoleService.getRoles(vo.getId());
            if (null != roles && !roles.isEmpty()) {
                vo.setRoles(roleConverter.toVOs(roles));
            }
            vo.setOrganizationName(organizationNameMap.getOrDefault(vo.getOrganizationId(), ""));
        });
        // 响应
        return result;
    }

    @Override
    public IPage<UserOnlineVO> online(PageFrom page) {
        return null;
    }
}
