package io.github.yangxj96.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelUserRole;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.mapper.RelUserRoleMapper;
import io.github.yangxj96.spectra.core.user.mapper.RoleMapper;
import io.github.yangxj96.spectra.core.user.service.RelUserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 关联服务-用户和角色
 */
@Service
public class RelUserRoleServiceImpl implements RelUserRoleService {

    @Resource
    private RelUserRoleMapper relUserRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    @Transactional
    public void grant(Long userId, List<Long> roleIds) {
        var coll = new ArrayList<RelUserRole>();
        for (Long roleId : roleIds) {
            coll.add(RelUserRole.builder().userId(userId).roleId(roleId).build());
        }
        relUserRoleMapper.insert(coll);
    }

    @Override
    @Transactional
    public void revoke(Long userId) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getUserId, userId);
        relUserRoleMapper.delete(wrapper);
    }

    @Override
    public void revoke(Long userId, List<Long> roleIds) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getUserId, userId)
                .in(RelUserRole::getRoleId, roleIds);
        relUserRoleMapper.delete(wrapper);
    }

    @Override
    public List<RelUserRole> getRelByRoleId(Long roleId) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>()
                .eq(RelUserRole::getRoleId, roleId);
        return relUserRoleMapper.selectList(wrapper);
    }

    @Override
    public List<Role> getRoles(Long userId) {
        var wrapper = new LambdaQueryWrapper<RelUserRole>();
        wrapper.eq(RelUserRole::getUserId, userId);
        List<RelUserRole> userRoles = relUserRoleMapper.selectList(wrapper);
        return roleMapper.selectByIds(userRoles.stream().map(RelUserRole::getRoleId).toList());
    }


}
