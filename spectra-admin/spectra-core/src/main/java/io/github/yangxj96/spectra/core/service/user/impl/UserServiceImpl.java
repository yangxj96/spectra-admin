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

import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.exception.DataSaveException;
import io.github.yangxj96.spectra.common.exception.EntityUpdateException;
import io.github.yangxj96.spectra.core.properties.UserProperties;
import io.github.yangxj96.spectra.core.javabean.system.entity.Organization;
import io.github.yangxj96.spectra.core.service.system.OrganizationService;
import io.github.yangxj96.spectra.core.javabean.user.converter.RoleConverter;
import io.github.yangxj96.spectra.core.javabean.user.converter.UserConverter;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.javabean.user.from.UserPageFrom;
import io.github.yangxj96.spectra.core.javabean.user.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserOnlineVO;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserPageVO;
import io.github.yangxj96.spectra.core.mapper.user.UserMapper;
import io.github.yangxj96.spectra.core.service.user.RelUserRoleService;
import io.github.yangxj96.spectra.core.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Override
    @Transactional
    public void create(UserSaveFrom params) {
        var entity = userConverter.toEntity(params);
        // 填充默认密码
        entity.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
        if (!this.save(entity)) {
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
        // 强制注销账号登录信息
        StpUtil.logout(user.getId());
        // 先删除角色关联
        relUserRoleService.revoke(user.getId());
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
        userConverter.updateUserFrom(params, entity);
        if (this.baseMapper.updateById(entity) == 0) {
            throw new EntityUpdateException("更新用户发生错误");
        }
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
            user.setPassword(passwordEncoder.encode(userProperties.getDefaultPassword()));
            this.baseMapper.updateById(user);
        } catch (Exception e) {
            log.error("用户不存在", e);
            throw new DataNotExistException("用户不存在");
        }
    }

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        var result = new Page<UserPageVO>();
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName())
                .like(StringUtils.isNotBlank(params.getEmail()), User::getEmail, params.getEmail())
                .eq(params.getStatus() != null, User::getState, params.getStatus());

        var db = this.page(page.toPage(), wrapper);
        BeanUtils.copyProperties(db, result);
        result.setRecords(userConverter.toVOs(db.getRecords()));

        // 获取所需内容
        var organizationNameMap = organizationService.list()
                .stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getPath));

        // vo扩展字段补充
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
    public User getByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    @Override
    public IPage<UserOnlineVO> online(PageFrom page) {
        var records = new ArrayList<UserOnlineVO>();

        // 查询所有在线的token信息
        var sessionIdS = StpUtil.searchSessionId(
                "",
                Math.toIntExact(((page.getPageNum() - 1) * page.getPageSize())),
                page.getPageSize().intValue(),
                false);

        for (String sessionId : sessionIdS) {
            var loginRecords = new ArrayList<UserOnlineVO.LoginRecordVo>();
            // 根据会话id，查询对应的 SaSession 对象，此处一个 SaSession 对象即代表一个登录的账号
            var session = StpUtil.getSessionBySessionId(sessionId);
            log.debug("登录ID:{}", session.getLoginId());
            // 查询这个账号都在哪些设备登录了，依据上面的示例，
            // 账号A 的 SaTerminalInfo 数量是 3，账号B 的 SaTerminalInfo 数量是 2
            var terminalList = session.terminalListCopy();
            log.debug("会话id：" + sessionId + "，共在 " + terminalList.size() + " 设备登录");
            for (SaTerminalInfo info : terminalList) {
                log.debug("分别是:{}", info);
                loginRecords.add(new UserOnlineVO.LoginRecordVo(
                        info.getTokenValue(),
                        info.getDeviceType(),
                        "255.255.255.255",
                        "内网地址",
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(info.getCreateTime()),
                                ZoneId.systemDefault()
                        )
                ));
            }
            User user = this.getById(Long.valueOf(session.getLoginId().toString()));
            records.add(new UserOnlineVO(
                    user.getEmail(),
                    user.getName(),
                    user.getOrganizationId().toString(),
                    loginRecords
            ));
        }

        var p = new Page<UserOnlineVO>();
        p.setCurrent(page.getPageNum());
        p.setSize(page.getPageSize());
        p.setRecords(records);
        p.setTotal(StpUtil.searchSessionId("", 0, -1, false).size());

        return p;
    }
}
