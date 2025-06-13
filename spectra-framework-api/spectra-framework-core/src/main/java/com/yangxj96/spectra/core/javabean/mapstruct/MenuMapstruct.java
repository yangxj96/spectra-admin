package com.yangxj96.spectra.core.javabean.mapstruct;

import com.yangxj96.spectra.core.javabean.entity.Menu;
import com.yangxj96.spectra.core.javabean.vo.MenuTreeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 菜单相关mapstruct
 *
 * @author Jack Young
 * @since 2025/6/13 15:16
 */
@Mapper(componentModel = "spring")
public interface MenuMapstruct {

    List<MenuTreeVO> toTreeVOS(List<Menu> menus);

    @Mapping(target = "children", ignore = true)
    MenuTreeVO toTreeVO(Menu menu);
}
