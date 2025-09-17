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

package io.github.yangxj96.spectra.core.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.exception.DataSaveException;
import io.github.yangxj96.spectra.common.exception.EntityUpdateException;
import io.github.yangxj96.spectra.core.auth.properties.UserProperties;
import io.github.yangxj96.spectra.core.system.javabean.entity.Organization;
import io.github.yangxj96.spectra.core.system.service.OrganizationService;
import io.github.yangxj96.spectra.core.user.javabean.entity.User;
import io.github.yangxj96.spectra.core.user.javabean.from.UserPageFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.user.javabean.mapstruct.PermissionMapstruct;
import io.github.yangxj96.spectra.core.user.javabean.mapstruct.UserMapstruct;
import io.github.yangxj96.spectra.core.user.javabean.vo.UserPageVO;
import io.github.yangxj96.spectra.core.user.mapper.UserMapper;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import io.github.yangxj96.spectra.core.user.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapstruct mapstruct;

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionMapstruct permissionMapstruct;

    @Resource
    private OrganizationService organizationService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    private UserProperties userProperties;

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        var result = new Page<UserPageVO>();
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName())
                .like(StringUtils.isNotBlank(params.getEmail()), User::getEmail, params.getEmail())
                .ne(BaseEntity::getId, StpUtil.getLoginIdAsLong())
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
        // 填充默认密码
        entity.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
        if (!this.save(entity)) {
            throw new DataSaveException("保存用户信息异常");
        }
        // 关联角色
        roleService.insertUserRel(entity.getId(), params.getRoleIds());
    }

    @Override
    @Transactional
    public void updateById(UserSaveFrom params) {
        var entity = this.getById(params.getId());
        if (null == entity) {
            throw new DataNotExistException("用户不存在");
        }
        mapstruct.updateUserFrom(params, entity);
        if (this.baseMapper.updateById(entity) == 0) {
            throw new EntityUpdateException("更新用户发生错误");
        }
        // 判断角色是否修改过,有角色就要判断下角色是否修改过了
        var currentRoles = new HashSet<>(roleService.getRoleIdsByUserId(params.getId()));
        var targetRoles = new HashSet<>(params.getRoleIds() != null ? params.getRoleIds() : List.of());

        // 计算要删除的
        var roleToDelete = new HashSet<>(currentRoles);
        roleToDelete.removeAll(targetRoles);

        if (!roleToDelete.isEmpty()) {
            List<Long> deleteList = List.copyOf(roleToDelete);
            if (roleService.removeUserRel(entity.getId(), deleteList) != deleteList.size()) {
                throw new EntityUpdateException("删除角色关联失败，未完全删除");
            }
        }

        // 计算要插入的角色
        var roleToInsert = new HashSet<>(targetRoles);
        roleToInsert.removeAll(currentRoles);

        if (!roleToInsert.isEmpty()) {
            List<Long> insertList = List.copyOf(roleToInsert);
            if (roleService.insertUserRel(entity.getId(), insertList) != insertList.size()) {
                throw new EntityUpdateException("新增角色关联失败，未完全插入");
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
        StpUtil.logout(user.getId());
        // 先删除角色关联
        roleService.removeUserRel(user.getId());
        // 删除用户信息
        this.removeById(user);
    }

    @Override
    @Transactional
    public void passwordResetById(String uid) {
        try {
            var user = this.getById(Long.parseLong(uid));
            user.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
            this.baseMapper.updateById(user);
        } catch (Exception e) {
            throw new DataNotExistException("用户不存在");
        }
    }

    @Override
    public User getByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }
}
