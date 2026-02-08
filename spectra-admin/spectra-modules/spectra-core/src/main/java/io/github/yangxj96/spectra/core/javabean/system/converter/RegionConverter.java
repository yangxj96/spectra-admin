package io.github.yangxj96.spectra.core.javabean.system.converter;


import io.github.yangxj96.spectra.core.configure.mapstruct.GlobalMapperConfig;
import io.github.yangxj96.spectra.core.configure.mapstruct.TimeMapper;
import io.github.yangxj96.spectra.core.javabean.system.entity.Region;
import io.github.yangxj96.spectra.core.javabean.system.vo.RegionVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 行政区划转换器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/2/2 15:50
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface RegionConverter {


    RegionVO toVO(Region source);


    List<RegionVO> toVOList(List<Region> source);

}
