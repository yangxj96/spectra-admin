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

package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.auth.service.AccountService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.converter.RoleConverter;
import com.devops00.spectra.core.user.javabean.converter.UserConverter;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.entity.UserDataScope;
import com.devops00.spectra.core.user.javabean.entity.UserDataScopeTarget;
import com.devops00.spectra.core.user.javabean.from.ChangePasswordFrom;
import com.devops00.spectra.core.user.javabean.from.UserPageFrom;
import com.devops00.spectra.core.user.javabean.from.UserProfileFrom;
import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.vo.UserPageVO;
import com.devops00.spectra.core.user.javabean.vo.UserProfileVO;
import com.devops00.spectra.core.user.mapper.UserDataScopeMapper;
import com.devops00.spectra.core.user.mapper.UserDataScopeTargetMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.mapper.RoleMapper;
import com.devops00.spectra.core.user.service.RelUserRoleService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.framework.assembler.NameFillExecutor;
import com.devops00.spectra.framework.configure.mvc.properties.UserProperties;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/// 用户service层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;

    private final RoleConverter roleConverter;

    private final RelUserRoleService relUserRoleService;

    private final RoleMapper roleMapper;

    private final DepartmentService departmentService;

    private final PasswordEncoder passwordEncoder;

    private final UserProperties userProperties;

    private final AccountService accountService;

    private final UserDataScopeMapper dataScopeMapper;

    private final UserDataScopeTargetMapper dataScopeTargetMapper;

    private final NameFillExecutor fillExecutor;

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
        var defaultAccount = new Account();
        defaultAccount.setUserId(entity.getId());
        defaultAccount.setType(LoginType.PASSWORD);
        defaultAccount.setLoginName(entity.getEmail());
        defaultAccount.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
        defaultAccount.setProvider("DEFAULT");
        defaultAccount.setStatus((short) 0);
        if (!accountService.save(defaultAccount)) {
            throw new DataSaveException("保存用户信息异常");
        }
        // 关联角色
        relUserRoleService.grant(entity.getId(), params.getRoleIds());
        // 更新用户数据范围
        this.updateUserScope(entity.getId(), params.getDataScope(), params.getTargetIds());
    }

    @Override
    @Transactional
    public void deleteById(UUID uid) {
        var user = this.getById(uid);
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        // 根据用户强制注销账号登录信息
        SecUtil.kick(user.getId());
        // 先删除角色关联
        relUserRoleService.revoke(user.getId());
        // 删除账号信息
        accountService.deleteByUserId(user.getId());
        // 删除用户信息
        this.removeById(user);
    }

    @Override
    @Transactional
    public void modify(UserSaveFrom params) {
        var entity = this.getById(params.getId());
        if (null == entity) {
            throw new DataNotExistException("用户不存在");
        }
        // 默认账号是否需要修改
        boolean defaultAccountUpdateFlag = params.getEmail().equals(entity.getEmail());
        userConverter.updateUser(params, entity);
        if (this.baseMapper.updateById(entity) == 0) {
            throw new EntityUpdateException("更新用户发生错误");
        }

        // 默认账号处理
        if (!defaultAccountUpdateFlag) {
            Account account = accountService.getDefaultByUserId(entity.getId());
            account.setLoginName(entity.getEmail());
            accountService.updateById(account);
        }

        // 数据范围修改
        this.updateUserScope(entity.getId(), params.getDataScope(), params.getTargetIds());

        // 角色处理
        // 判断角色是否修改过,有角色就要判断下角色是否修改过了
        var currentRoles = new HashSet<>(relUserRoleService.getRoles(params.getId()).stream().map(Role::getId).toList());
        var targetRoles = new HashSet<>(params.getRoleIds() != null ? params.getRoleIds() : List.of());

        // 计算要删除的
        var roleToDelete = new HashSet<>(currentRoles);
        roleToDelete.removeAll(targetRoles);

        if (!roleToDelete.isEmpty()) {
            List<UUID> deleteList = List.copyOf(roleToDelete);
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
            List<UUID> insertList = List.copyOf(roleToInsert);
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
    public void replaceRoles(UUID userId, List<UUID> roleIds) {
        if (this.getById(userId) == null) {
            throw new DataNotExistException("用户不存在");
        }
        var targetIds = new HashSet<>(roleIds == null ? List.<UUID>of() : roleIds);
        if (targetIds.isEmpty()) {
            throw new DataException("用户至少需要关联一个角色");
        }
        var activeRoleCount = roleMapper
                .selectCount(new LambdaQueryWrapper<Role>().in(Role::getId, targetIds).eq(Role::getState, Boolean.TRUE).isNull(Role::getDeleted));
        if (activeRoleCount != targetIds.size()) {
            throw new DataNotExistException("存在无效或禁用角色");
        }
        relUserRoleService.revoke(userId);
        relUserRoleService.grant(userId, List.copyOf(targetIds));
    }

    @Override
    @Transactional
    public void passwordResetById(UUID uid) {
        try {
            var user = this.getById(uid);
            Account account = accountService.getDefaultByUserId(user.getId());
            account.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
            accountService.updateById(account);
        } catch (Exception e) {
            log.error("用户不存在", e);
            throw new DataNotExistException("用户不存在");
        }
    }

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) throws IllegalAccessException {
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>().like(StrUtils.isNotBlank(params.getUsername()), User::getUsername, params.getUsername())
                .like(StrUtils.isNotBlank(params.getEmail()), User::getEmail, params.getEmail())
                .in(params.getDepartmentId() != null, User::getDepartmentId, departmentService.getSelfAndDescendantIds(params.getDepartmentId()))
                .eq(params.getStatus() != null, User::getStatus, params.getStatus());

        var db = this.page(page.toPage(), wrapper);
        var result = userConverter.toVOPage(db);

        // 字段填充
        fillExecutor.fill(result.getRecords());

        // 扩展字段补充
        result.getRecords().forEach(vo -> {
            var roles = relUserRoleService.getRoles(vo.getId());
            if (null != roles && !roles.isEmpty()) {
                vo.setRoles(roles.stream().map(roleConverter::toVO).toList());
            }
            // 补充数据范围
            var scope = dataScopeMapper.findByUserId(vo.getId());
            if (scope != null) {
                vo.setDataScope(scope.getScopeType());
                if (scope.getScopeType() == DataScopeType.CUSTOM) {
                    var targets = dataScopeTargetMapper.findByUserId(vo.getId());
                    vo.setTargetIds(targets.stream().map(target -> target.getTargetId().toString()).toList());
                }
            }
        });
        // 响应
        return result;
    }

    @Override
    public List<UserOnlineVO> online(PageFrom page) {
        return SecUtil.online();
    }

    @Override
    public UserProfileVO getProfile(UUID userId) {
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        var vo = userConverter.toProfileVO(user);
        // 填充部门名称
        if (user.getDepartmentId() != null) {
            var dept = departmentService.getById(user.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getName());
            }
        }
        // 填充角色列表
        var roles = relUserRoleService.getRoles(userId);
        if (roles != null && !roles.isEmpty()) {
            vo.setRoles(roles.stream().map(roleConverter::toVO).toList());
        }
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(UUID userId, UserProfileFrom params) {
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }
        userConverter.updateProfile(params, user);
        if (this.baseMapper.updateById(user) == 0) {
            throw new EntityUpdateException("更新用户信息失败");
        }
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordFrom params) {
        // 1. 获取用户
        var user = this.getById(userId);
        if (user == null) {
            throw new DataNotExistException("用户不存在");
        }

        // 2. 获取用户的默认账号
        var account = accountService.getDefaultByUserId(userId);
        if (account == null) {
            throw new DataNotExistException("账号不存在");
        }

        // 3. 验证旧密码
        if (!passwordEncoder.matches(params.getOldPassword(), account.getPassword())) {
            throw new SpectraException("旧密码错误");
        }

        // 4. 验证新密码和确认密码是否一致
        if (!params.getNewPassword().equals(params.getVerifyPassword())) {
            throw new SpectraException("两次输入的新密码不一致");
        }

        // 5. 验证新密码不能与旧密码相同
        if (passwordEncoder.matches(params.getNewPassword(), account.getPassword())) {
            throw new SpectraException("新密码不能与旧密码相同");
        }

        // 6. 加密新密码并更新
        account.setPassword(passwordEncoder.encode(params.getNewPassword()));
        if (!accountService.updateById(account)) {
            throw new EntityUpdateException("修改密码失败");
        }
        log.info("用户 {} 修改密码成功", userId);
    }

    /// 更新用户数据范围
    ///
    /// @param userId    用户ID
    /// @param type      权限范围类型
    /// @param targetIds 自定义权限范围
    private void updateUserScope(UUID userId, DataScopeType type, List<UUID> targetIds) {
        // null 表示继承角色范围，不能偷偷转换成 DEPT 覆盖角色。
        if (type == null) {
            dataScopeMapper.removeByUserId(userId);
            dataScopeTargetMapper.removeByUserId(userId);
            return;
        }
        if (type == DataScopeType.GLOBAL && !canManageGlobalScope()) {
            throw new DataScopeViolationException("只有系统运维角色可以授予 GLOBAL 数据范围");
        }
        if (type == DataScopeType.CUSTOM && CollUtils.isEmpty(targetIds)) {
            throw new DataScopeViolationException("CUSTOM 数据范围必须指定至少一个目标部门");
        }
        // 更新或新增用户的权限范围
        var scope = dataScopeMapper.findByUserId(userId);
        if (scope == null) {
            scope = new UserDataScope();
        }
        scope.setUserId(userId);
        scope.setScopeType(type);
        dataScopeMapper.insertOrUpdate(scope);

        // 先清空自定义范围
        dataScopeTargetMapper.removeByUserId(userId);
        // 如果为自定义则添加新的自定义范围
        if (type == DataScopeType.CUSTOM && CollUtils.isNotEmpty(targetIds)) {
            var targets = new ArrayList<UserDataScopeTarget>();
            for (UUID targetId : targetIds) {
                var target = new UserDataScopeTarget();
                target.setTargetId(targetId);
                target.setUserId(userId);
                target.setTargetType(type.getCode());

                targets.add(target);
            }
            dataScopeTargetMapper.insert(targets);
        }
    }

    private boolean canManageGlobalScope() {
        var currentUser = SecUtil.getCurrentUser();
        return currentUser != null
            && currentUser.getAuthorities()
                    .stream()
                    .anyMatch(authority -> "ROLE_DEV_OPS".equals(authority.getAuthority()) || "*".equals(authority.getAuthority()));
    }
}
