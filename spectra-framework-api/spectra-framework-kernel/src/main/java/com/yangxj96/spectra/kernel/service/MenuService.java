package com.yangxj96.spectra.kernel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
import com.yangxj96.spectra.kernel.entity.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单业务层
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
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
