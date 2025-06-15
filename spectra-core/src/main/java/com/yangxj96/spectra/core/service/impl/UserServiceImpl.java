package com.yangxj96.spectra.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.common.exception.DataNotExistException;
import com.yangxj96.spectra.core.javabean.entity.User;
import com.yangxj96.spectra.core.javabean.from.UserPageFrom;
import com.yangxj96.spectra.core.javabean.from.UserRelevanceRolesFrom;
import com.yangxj96.spectra.core.javabean.mapstruct.RoleMapstruct;
import com.yangxj96.spectra.core.javabean.mapstruct.UserMapstruct;
import com.yangxj96.spectra.core.javabean.vo.RoleVO;
import com.yangxj96.spectra.core.javabean.vo.UserPageVO;
import com.yangxj96.spectra.core.mapper.UserMapper;
import com.yangxj96.spectra.core.service.RoleService;
import com.yangxj96.spectra.core.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

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
    private RoleMapstruct roleMapstruct;

    @Resource
    private RoleService roleService;

    @Override
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        var result = new Page<UserPageVO>();
        // 条件构建
        var wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.isNotBlank(params.getName()), User::getName, params.getName());
        Page<User> db = this.page(new Page<>(page.getPageNum(), page.getPageSize()), wrapper);
        BeanUtils.copyProperties(db, result);
        result.setRecords(mapstruct.toVOs(db.getRecords()));
        // vo扩展字段补充
        result.getRecords().forEach(vo -> {
            var roles = roleService.getByUserId(vo.getId());
            if (null != roles && !roles.isEmpty()) {
                vo.setRoles((ArrayList<RoleVO>) roleMapstruct.toVOs(roles));
            }
        });
        // 响应
        return result;
    }

    @Override
    @Transactional
    public void relevanceRoles(UserRelevanceRolesFrom params) {
        var user = this.getById(params.getUserId());
        if (null == user) {
            throw new DataNotExistException("用户不存在");
        }
        var roles = roleService.listByIds(params.getRoleIds());
        if (roles.isEmpty() || roles.size() != params.getRoleIds().size()) {
            throw new DataNotExistException("角色列表不正确");
        }
        // 移除之前的关联
        this.baseMapper.removeRelevanceRoles(params.getUserId());
        for (Long roleId : params.getRoleIds()) {
            this.baseMapper.insertRelevanceRoles(IdWorker.getId(), params.getUserId(), roleId, StpUtil.getLoginIdAsLong());
        }
    }

}
