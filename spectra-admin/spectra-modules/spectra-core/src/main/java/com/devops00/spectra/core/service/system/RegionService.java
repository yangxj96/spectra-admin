package com.devops00.spectra.core.service.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.javabean.system.entity.Region;
import com.devops00.spectra.core.javabean.system.from.RegionPageFrom;
import com.devops00.spectra.core.javabean.system.vo.RegionVO;

import java.util.List;

/// 行政区域Service
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 13:57
public interface RegionService extends BaseService<Region> {

    /// 懒加载树
    ///
    /// @param level 层级
    /// @param id    父级ID
    /// @return 根据条件获取的下级的列表
    List<RegionVO> lazyTree(Integer level, String id);

    /// 分页查询行政区划
    ///
    /// @param page   分页信息
    /// @param params 过滤参数
    /// @return 分页响应信息
    IPage<RegionVO> page(PageFrom page, RegionPageFrom params);

}
