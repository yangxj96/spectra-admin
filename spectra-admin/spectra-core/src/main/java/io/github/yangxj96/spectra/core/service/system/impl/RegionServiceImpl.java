package io.github.yangxj96.spectra.core.service.system.impl;


import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.javabean.system.entity.Region;
import io.github.yangxj96.spectra.core.mapper.system.RegionMapper;
import io.github.yangxj96.spectra.core.service.system.RegionService;
import org.springframework.stereotype.Service;

/// 行政区域实现Service
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 13:57
@Service
public class RegionServiceImpl extends BaseServiceImpl<RegionMapper, Region> implements RegionService {
}
