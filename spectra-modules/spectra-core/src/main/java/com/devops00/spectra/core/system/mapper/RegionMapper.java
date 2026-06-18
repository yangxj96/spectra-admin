package com.devops00.spectra.core.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.system.javabean.entity.Region;
import org.apache.ibatis.annotations.Mapper;

/// 行政区域Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 11:49
@Mapper
public interface RegionMapper extends BaseMapper<Region> {
}
