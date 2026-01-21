package io.github.yangxj96.spectra.core.service.auth.impl;

import io.github.yangxj96.spectra.core.configure.datascope.DataScopeContext;
import io.github.yangxj96.spectra.core.configure.datascope.DataScopeType;
import io.github.yangxj96.spectra.core.javabean.user.entity.RoleDataScope;
import io.github.yangxj96.spectra.core.javabean.user.entity.UserDataScope;
import io.github.yangxj96.spectra.core.javabean.user.entity.UserDataScopeTarget;
import io.github.yangxj96.spectra.core.mapper.user.RoleDataScopeMapper;
import io.github.yangxj96.spectra.core.mapper.user.UserDataScopeMapper;
import io.github.yangxj96.spectra.core.mapper.user.UserDataScopeTargetMapper;
import io.github.yangxj96.spectra.core.service.auth.DataScopeService;
import org.springframework.stereotype.Service;

import java.util.List;

/// 数据范围业务层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 11:28
@Service
public class DataScopeServiceImpl implements DataScopeService {

    private final UserDataScopeMapper userDataScopeMapper;

    private final UserDataScopeTargetMapper userDataScopeTargetMapper;

    private final RoleDataScopeMapper roleDataScopeMapper;

    public DataScopeServiceImpl(UserDataScopeMapper userDataScopeMapper, UserDataScopeTargetMapper userDataScopeTargetMapper, RoleDataScopeMapper roleDataScopeMapper) {
        this.userDataScopeMapper = userDataScopeMapper;
        this.userDataScopeTargetMapper = userDataScopeTargetMapper;
        this.roleDataScopeMapper = roleDataScopeMapper;
    }

    @Override
    public DataScopeContext resolve(String userId) {
        // 用户直授（最高优先级）
        DataScopeContext direct = resolveUser(userId);
        if (direct != null) {
            return direct;
        }

        // 角色权限
        DataScopeContext roleScope = resolveRole(userId);
        if (roleScope != null) {
            return roleScope;
        }

        // 默认策略（兜底）本人数据
        return DataScopeContext.builder()
                .userId(userId)
                .scope(DataScopeType.SELF)
                .build();
    }

    public DataScopeContext resolveUser(String userId) {
        // TODO 查询用户权限范围还没完善
        UserDataScope entity = userDataScopeMapper.findByUserId(userId);
        if (entity == null) {
            return null;
        }
        List<UserDataScopeTarget> targets = userDataScopeTargetMapper.findByUserId(userId);

        return DataScopeContext.builder()
                .userId(userId)
                .scope(entity.getScopeType())
                .userField("CREATED_BY")
                .targetIds(targets.stream().map(UserDataScopeTarget::getTargetId).toList())
                .build();
    }

    public DataScopeContext resolveRole(String userId) {
        // TODO 查询角色权限范围还没完善
        RoleDataScope scope = roleDataScopeMapper.findByRoleId(userId);
        if (scope == null) {
            return null;
        }

        return DataScopeContext.builder()
                .userId(userId)
                .scope(scope.getScopeType())
                .scopeField("org_id")   // 强约定
                //.targetIds(scope.getDeptIds())
                .build();
    }

}
