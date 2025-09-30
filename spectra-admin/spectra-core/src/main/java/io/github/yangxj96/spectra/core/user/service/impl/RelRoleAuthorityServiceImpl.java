package io.github.yangxj96.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.utils.TreeUtils;
import io.github.yangxj96.spectra.core.user.javabean.converter.AuthorityConverter;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleAuthority;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleAuthorityMapper;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import io.github.yangxj96.spectra.core.user.service.RelRoleAuthorityService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关联服务-用户和权限
 */
@Service
public class RelRoleAuthorityServiceImpl implements RelRoleAuthorityService {

    @Resource
    private RelRoleAuthorityMapper relRoleAuthorityMapper;

    @Resource
    private AuthorityService authorityService;

    @Resource
    private AuthorityConverter authorityConverter;

    @Override
    @Transactional
    public void grant(Long roleId, RoleAuthorityFrom from) {
        // 压缩权限树
        from.setAuthorityIds(
                TreeUtils.compressSelectedNodes(
                        authorityService.tree(),
                        new HashSet<>(from.getAuthorityIds()),
                        AuthorityTreeVO::getId
                ).stream().toList()
        );
        // 开始进入修改权限的具体执行方法
        var currentIds = relRoleAuthorityMapper.getByRoleId(roleId)
                .stream().map(RelRoleAuthority::getAuthorityId).collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getAuthorityIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleAuthority>()
                    .eq(RelRoleAuthority::getRoleId, roleId)
                    .in(RelRoleAuthority::getAuthorityId, removeIds);
            relRoleAuthorityMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds); // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleAuthority> newRelations = addIds.stream()
                    .map(addId -> RelRoleAuthority.builder()
                            .roleId(roleId)
                            .authorityId(addId)
                            .build())
                    .collect(Collectors.toList());
            relRoleAuthorityMapper.insert(newRelations);
        }
    }

    @Override
    @Transactional
    public void revoke(Long roleId) {
        // 删除角色关联的权限
        var wrapper = new LambdaQueryWrapper<RelRoleAuthority>().eq(RelRoleAuthority::getRoleId, roleId);
        relRoleAuthorityMapper.delete(wrapper);
    }

    @Override
    public List<AuthorityVO> get(Long roleId) {
        List<Authority> authority = authorityService.getByRelRoleId(roleId);
        return authorityConverter.toVOS(authority);
    }

    @Override
    public List<AuthorityVO> get(List<Long> ids) {
        List<RelRoleAuthority> relRoleAuthorities = relRoleAuthorityMapper.selectList(
                new LambdaQueryWrapper<RelRoleAuthority>()
                        .in(RelRoleAuthority::getRoleId, ids)
        );
        if (relRoleAuthorities == null || CollectionUtils.isEmpty(relRoleAuthorities)) {
            return new ArrayList<>();
        }
        List<Long> authorityIds = relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList();
        var coll = authorityService.list(new LambdaQueryWrapper<Authority>().in(BaseEntity::getId, authorityIds));
        return authorityConverter.toVOS(coll);
    }


}
