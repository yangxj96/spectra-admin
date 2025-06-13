package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.common.constant.Common;
import com.yangxj96.spectra.common.exception.DataNotExistException;
import com.yangxj96.spectra.common.utils.TreeBuilder;
import com.yangxj96.spectra.core.javabean.entity.Menu;
import com.yangxj96.spectra.core.javabean.from.MenuSaveFrom;
import com.yangxj96.spectra.core.javabean.mapstruct.MenuMapstruct;
import com.yangxj96.spectra.core.javabean.vo.MenuTreeVO;
import com.yangxj96.spectra.core.mapper.MenuMapper;
import com.yangxj96.spectra.core.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单service层-实现
 *
 * @author Jack Young
 * @since 2025/6/3 23:18
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Resource
    private MenuMapstruct mapstruct;

    @Override
    public List<MenuTreeVO> tree() {
        List<Menu> menus = this.list();
        // 先转树形VO
        List<MenuTreeVO> vos = mapstruct.toTreeVOS(menus);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
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
