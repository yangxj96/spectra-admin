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

package com.devops00.spectra.core.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devops00.spectra.common.assembler.NameFillExecutor;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.configure.mvc.properties.UserProperties;
import com.devops00.spectra.core.javabean.auth.entity.Account;
import com.devops00.spectra.core.javabean.user.converter.RoleConverter;
import com.devops00.spectra.core.javabean.user.converter.UserConverter;
import com.devops00.spectra.core.javabean.user.entity.Role;
import com.devops00.spectra.core.javabean.user.entity.User;
import com.devops00.spectra.core.javabean.user.entity.UserDataScope;
import com.devops00.spectra.core.javabean.user.from.UserPageFrom;
import com.devops00.spectra.core.javabean.user.from.UserSaveFrom;
import com.devops00.spectra.core.javabean.user.vo.UserPageVO;
import com.devops00.spectra.core.mapper.user.UserDataScopeMapper;
import com.devops00.spectra.core.mapper.user.UserDataScopeTargetMapper;
import com.devops00.spectra.core.mapper.user.UserMapper;
import com.devops00.spectra.core.service.auth.AccountService;
import com.devops00.spectra.core.service.system.DepartmentService;
import com.devops00.spectra.core.service.user.RelUserRoleService;
import com.devops00.spectra.core.service.user.UserService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/// 用户service层-实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Slf4j
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;

    private final RoleConverter roleConverter;

    private final RelUserRoleService relUserRoleService;

    private final DepartmentService departmentService;

    private final PasswordEncoder passwordEncoder;

    private final UserProperties userProperties;

    private final AccountService accountService;

    private final UserDataScopeMapper dataScopeMapper;

    private final UserDataScopeTargetMapper dataScopeTargetMapper;

    private final NameFillExecutor fillExecutor;

    public UserServiceImpl(UserConverter userConverter, RoleConverter roleConverter, RelUserRoleService relUserRoleService, DepartmentService departmentService, PasswordEncoder passwordEncoder, UserProperties userProperties, AccountService accountService, UserDataScopeMapper dataScopeMapper, UserDataScopeTargetMapper dataScopeTargetMapper, NameFillExecutor fillExecutor) {
        this.userConverter = userConverter;
        this.roleConverter = roleConverter;
        this.relUserRoleService = relUserRoleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.userProperties = userProperties;
        this.accountService = accountService;
        this.dataScopeMapper = dataScopeMapper;
        this.dataScopeTargetMapper = dataScopeTargetMapper;
        this.fillExecutor = fillExecutor;
    }


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

        // 数据范围处理
        if (params.getDataScope() != null) {
            // 新增
            var dataScopeEntity = new UserDataScope();
            dataScopeEntity.setUserId(entity.getId());
            dataScopeEntity.setScopeType(params.getDataScope());
            dataScopeMapper.insert(dataScopeEntity);

            // TODO 如果是自定义的话,要插入自定义的数据
            //if (params.getDataScope() == DataScopeType.CUSTOM) {
            //    var targets = new ArrayList<UserDataScopeTarget>();
            //    for (String targetId : params.getTargetIds()) {
            //        var datum = new UserDataScopeTarget();
            //        datum.setUserId(entity.getId());
            //        datum.setTargetId(targetId);
            //        datum.setTargetType(0);
            //        targets.add(datum);
            //    }
            //    dataScopeTargetMapper.insert(targets);
            //}
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
    public void updateById(UserSaveFrom params) {
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

        // 数据范围处理
        UserDataScope dataScope = dataScopeMapper.findByUserId(entity.getId());
        if (params.getDataScope() == null && dataScope != null) {
            // 请求参数不存在数据范围,但是数据范围数据存在,则删除数据范围数据
            dataScopeMapper.deleteById(dataScope.getId());
        }
        //if (params.getDataScope() != null) {
        //    if (dataScope == null) {
        //        // 新增
        //        var dataScopeEntity = new UserDataScope();
        //        dataScopeEntity.setUserId(entity.getId());
        //        dataScopeEntity.setScopeType(params.getDataScope());
        //        dataScopeMapper.insert(dataScopeEntity);
        //    } else {
        //        // 如果之前存的是CUSTOM,则需要清除一下
        //        if (dataScope.getScopeType().equals(DataScopeType.CUSTOM)) {
        //            List<UserDataScopeTarget> userDataScopeTargets = dataScopeTargetMapper.selectList(
        //                    new LambdaQueryWrapper<UserDataScopeTarget>()
        //                            .eq(UserDataScopeTarget::getUserId, entity.getId())
        //            );
        //            dataScopeTargetMapper.deleteByIds(
        //                    userDataScopeTargets.stream()
        //                            .map(BaseEntity::getId)
        //                            .toList()
        //            );
        //        }
        //        dataScope.setScopeType(params.getDataScope());
        //        dataScopeMapper.updateById(dataScope);
        //    }
        //
        //    // 如果现在修改成了自定义的话,要插入自定义的数据
        //    if (params.getDataScope() == DataScopeType.CUSTOM) {
        //        var targets = new ArrayList<UserDataScopeTarget>();
        //        for (String targetId : params.getTargetIds()) {
        //            var datum = new UserDataScopeTarget();
        //            datum.setUserId(entity.getId());
        //            datum.setTargetId(targetId);
        //            datum.setTargetType(0);
        //            targets.add(datum);
        //        }
        //        dataScopeTargetMapper.insert(targets);
        //    }
        //}


        // 角色处理
        // 判断角色是否修改过,有角色就要判断下角色是否修改过了
        var currentRoles = new HashSet<>(relUserRoleService.getRoles(params.getId()).stream().map(Role::getId).toList());
        var targetRoles = new HashSet<>(params.getRoleIds() != null ? params.getRoleIds() : List.of());

        // 计算要删除的
        var roleToDelete = new HashSet<>(currentRoles);
        roleToDelete.removeAll(targetRoles);

        if (!roleToDelete.isEmpty()) {
            List<String> deleteList = List.copyOf(roleToDelete);
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
            List<String> insertList = List.copyOf(roleToInsert);
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
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StrUtils.isNotBlank(params.getUsername()), User::getUsername, params.getUsername())
                .like(StrUtils.isNotBlank(params.getEmail()), User::getEmail, params.getEmail())
                .in(StrUtils.isNotBlank(
                                params.getDepartmentId()),
                        User::getDepartmentId,
                        departmentService.getSelfAndDescendantIds(params.getDepartmentId())
                )
                .eq(params.getStatus() != null, User::getStatus, params.getStatus());

        var db = this.page(page.toPage(), wrapper);
        var result = userConverter.toVOPage(db);

        // 字段填充
        fillExecutor.fill(result.getRecords());

        // 扩展字段补充
        result.getRecords().forEach(vo -> {
            var roles = relUserRoleService.getRoles(vo.getId());
            if (null != roles && !roles.isEmpty()) {
                vo.setRoles(
                        roles.stream()
                                .map(roleConverter::toVO)
                                .toList()
                );
            }
            // 数据范围
            //UserDataScope dataScope = dataScopeMapper.findByUserId(vo.getId());
            //if (dataScope != null) {
            //    vo.setDataScope(dataScope.getScopeType());
            //    if (dataScope.getScopeType().equals(1)) {
            //        List<UserDataScopeTarget> targets = dataScopeTargetMapper.findByUserId(vo.getId());
            //        vo.setTargetIds(
            //                targets.stream()
            //                        .map(UserDataScopeTarget::getTargetId)
            //                        .map(Object::toString)
            //                        .toList()
            //        );
            //    }
            //}
        });
        // 响应
        return result;
    }

    @Override
    public List<UserOnlineVO> online(PageFrom page) {
        return SecUtil.online();
    }
}
