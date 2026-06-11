package com.devops00.spectra.core.javabean.system.converter;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.javabean.system.entity.Region;
import com.devops00.spectra.core.javabean.system.vo.RegionVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/// 行政区划转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/2 15:50
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface RegionConverter {


    RegionVO toVO(Region source);


    List<RegionVO> toVOList(List<Region> source);

    /// 转换到分页的VO信息
    ///
    /// @param source 分页信息
    /// @return IPAGE
    @Mapping(target = "pages", ignore = true)
    Page<RegionVO> toVOPage(Page<Region> source);

}
