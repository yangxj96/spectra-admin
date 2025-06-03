package com.yangxj96.spectra.kernel.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.mapper.MenuMapper;
import com.yangxj96.spectra.kernel.service.MenuService;
import org.springframework.stereotype.Service;

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
    public IPage<Menu> page(PageFrom page) {
        Page<Menu> p = new Page<>(page.getPageNum(), page.getPageSize());
        return this.page(p, new LambdaQueryWrapper<>());
    }

    @Override
    public List<Tree<String>> tree() {
        List<Menu> menus = this.list();
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("sort");
        config.setParentIdKey("pid");
        return TreeUtil.build(menus, "0", config, (node, tree) -> {
            tree.setId(node.getId().toString());
            tree.setParentId(node.getPid().toString());
            tree.setName(node.getName());
            tree.putExtra("icon", node.getIcon());
            tree.putExtra("path", node.getPath());
            tree.putExtra("component", node.getComponent());
            tree.putExtra("layout", node.getLayout());
            tree.putExtra("sort", node.getSort());
        });
    }
}
