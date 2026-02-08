package io.github.yangxj96.spectra.core.service.system;


import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.common.constant.RegionLevel;
import io.github.yangxj96.spectra.core.javabean.system.entity.Region;
import io.github.yangxj96.spectra.core.javabean.system.vo.RegionVO;

import java.util.List;

/// 行政区域Service
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 13:57
public interface RegionService extends BaseService<Region>  {

    /// 懒加载树
    ///
    /// @param level 层级
    /// @param id    父级ID
    /// @return 根据条件获取的下级的列表
    List<RegionVO> lazyTree(Integer level, String id);

}
