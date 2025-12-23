package io.github.yangxj96.spectra.core.service.auth.impl;

import io.github.yangxj96.spectra.core.configure.datascope.DataScopeContext;
import io.github.yangxj96.spectra.core.configure.datascope.DataScopeType;
import io.github.yangxj96.spectra.core.javabean.user.entity.RoleDataScope;
import io.github.yangxj96.spectra.core.javabean.user.entity.UserDataScope;
import io.github.yangxj96.spectra.core.mapper.user.RoleDataScopeMapper;
import io.github.yangxj96.spectra.core.mapper.user.UserDataScopeMapper;
import io.github.yangxj96.spectra.core.service.auth.DataScopeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据范围业务层
 */
@Service
public class DataScopeServiceImpl implements DataScopeService {


    // TODO 未完善,当前做测试


    @Resource
    private UserDataScopeMapper userDataScopeMapper;

    @Resource
    private RoleDataScopeMapper roleDataScopeMapper;

    @Override
    public DataScopeContext resolve(Long userId) {
        // 这里直接复用你之前已经设计好的：
        // 用户直授 > 角色 > 默认
        // 1️⃣ 用户直授（最高优先级）
        DataScopeContext direct = resolveUser(userId);
        if (direct != null) {
            return direct;
        }

        // 2️⃣ 角色权限
        DataScopeContext roleScope = resolveRole(userId);
        if (roleScope != null) {
            return roleScope;
        }

        // 3️⃣ 默认策略（兜底）
        // 获取用户所属部门
        // Long deptId = userDeptService.getDeptId(userId);

        return DataScopeContext.builder()
                .userId(userId)
                .scope(DataScopeType.DEPT)          // 默认：本部门
                .scopeField("org_id")               // 表字段
                .targetIds(List.of(0L))         // 部门 ID
                .build();
    }

    public DataScopeContext resolveUser(Long userId) {
        UserDataScope entity = userDataScopeMapper.findByUserId(userId);
        if (entity == null) {
            return null;
        }

        return DataScopeContext.builder()
                .userId(userId)
                .scope(entity.getScopeType())
                .scopeField("o.org_id")   // 如 o.org_id
                .userField("o.created_by")     // 如 o.created_by
                // .targetIds(entity.getTargetIds())
                .build();
    }

    public DataScopeContext resolveRole(Long userId) {
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
