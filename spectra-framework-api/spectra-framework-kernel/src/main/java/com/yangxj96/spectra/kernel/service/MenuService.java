package com.yangxj96.spectra.kernel.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.kernel.entity.dto.Menu;

import java.util.List;

/**
 * 菜单业务层
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
 */
public interface MenuService extends IService<Menu> {

    /**
     * 分页查询数据
     *
     * @param page 分页信息
     * @return 分页VO
     */
    IPage<Menu> page(PageFrom page);

    /**
     * 生成树形菜单
     *
     * @return 生成的树形菜单
     */
    List<Tree<String>> tree();

}
