package io.github.yangxj96.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.core.system.javabean.converter.MenuConverter;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.system.service.MenuService;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleMenu;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.user.service.RelRoleMenuService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关联服务-角色和菜单
 */
@Service
public class RelRoleMenuServiceImpl implements RelRoleMenuService {

    @Resource
    private RelRoleMenuMapper relRoleMenuMapper;

    @Resource
    private MenuService menuService;

    @Resource
    private MenuConverter menuConverter;

    @Override
    @Transactional
    public void grant(Long roleId, RoleMenuFrom from) {
        // 当前角色关联的菜单信息
        var currentIds = relRoleMenuMapper.getByRoleId(roleId)
                .stream().map(RelRoleMenu::getMenuId).collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getMenuIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleMenu>()
                    .eq(RelRoleMenu::getRoleId, roleId)
                    .in(RelRoleMenu::getRoleId, removeIds);
            relRoleMenuMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds);  // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleMenu> newMenu = addIds.stream()
                    .map(addId -> RelRoleMenu.builder()
                            .roleId(roleId)
                            .menuId(addId)
                            .build())
                    .collect(Collectors.toList());
            relRoleMenuMapper.insert(newMenu);
        }
    }

    @Override
    @Transactional
    public void revoke(Long roleId) {
        // 删除角色关联的菜单
        var wrapper = new LambdaQueryWrapper<RelRoleMenu>().eq(RelRoleMenu::getRoleId, roleId);
        relRoleMenuMapper.delete(wrapper);
    }

    @Override
    public List<MenuVO> get(Long roleId) {
        List<Menu> menus = menuService.getByRelRoleId(roleId);
        return menuConverter.toVOS(menus);
    }

}
