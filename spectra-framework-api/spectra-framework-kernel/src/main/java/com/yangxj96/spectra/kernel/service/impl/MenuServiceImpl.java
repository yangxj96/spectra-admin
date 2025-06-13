package com.yangxj96.spectra.kernel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.exception.DataNotExistException;
import com.yangxj96.spectra.core.utils.TreeBuildConfig;
import com.yangxj96.spectra.core.utils.TreeNode;
import com.yangxj96.spectra.core.utils.TreeUtils;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
import com.yangxj96.spectra.kernel.mapper.MenuMapper;
import com.yangxj96.spectra.kernel.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单业务层实现
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<TreeNode> tree() {
        List<Menu> menus = this.list();
        // 构建树配置
        TreeBuildConfig<Menu> config = TreeBuildConfig.<Menu>builder()
                .idFunc(menu -> menu.getId() == null ? null : menu.getId().toString())
                .parentIdFunc(menu -> menu.getPid() == null ? null : menu.getPid().toString())
                .nodeMapper((menu, treeNode) -> {
                    treeNode.setName(menu.getName());
                    treeNode.getExtra().put("icon", menu.getIcon());
                    treeNode.getExtra().put("path", menu.getPath());
                    treeNode.getExtra().put("component", menu.getComponent());
                    treeNode.getExtra().put("layout", menu.getLayout());
                    treeNode.getExtra().put("sort", menu.getSort());
                })
                .comparator(TreeBuildConfig.defaultComparator())
                .skipIfParentNotExists(false)
                .build();
        return TreeUtils.buildTree(menus, "0", config);
    }

    @Override
    @Transactional
    public void created(MenuSaveFrom params) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(params, menu);
        this.save(menu);
    }

    @Override
    @Transactional
    public void modify(MenuSaveFrom params) {
        if (null == this.getById(params.getId())) {
            throw new DataNotExistException("[" + params.getId() + "]不存在");
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(params, menu);
        this.updateById(menu);
    }
}
