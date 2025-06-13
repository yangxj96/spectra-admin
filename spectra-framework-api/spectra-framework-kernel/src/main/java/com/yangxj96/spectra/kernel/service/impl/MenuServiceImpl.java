package com.yangxj96.spectra.kernel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.constant.Common;
import com.yangxj96.spectra.core.exception.DataNotExistException;
import com.yangxj96.spectra.core.utils.TreeBuilder;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
import com.yangxj96.spectra.kernel.entity.mapstruct.MenuMapstruct;
import com.yangxj96.spectra.kernel.entity.vo.MenuTreeVO;
import com.yangxj96.spectra.kernel.mapper.MenuMapper;
import com.yangxj96.spectra.kernel.service.MenuService;
import jakarta.annotation.Resource;
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
