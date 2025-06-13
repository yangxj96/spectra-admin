package com.yangxj96.spectra.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yangxj96.spectra.core.javabean.entity.Menu;
import com.yangxj96.spectra.core.javabean.from.MenuSaveFrom;
import com.yangxj96.spectra.core.javabean.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface MenuService extends IService<Menu> {

    /**
     * 生成树形菜单
     *
     * @return 生成的树形菜单
     */
    List<MenuTreeVO> tree();

    /**
     * 创建菜单
     *
     * @param params 菜单信息
     */
    void created(MenuSaveFrom params);

    /**
     * 修改菜单信息
     *
     * @param params 修改参数
     */
    void modify(MenuSaveFrom params);
}
