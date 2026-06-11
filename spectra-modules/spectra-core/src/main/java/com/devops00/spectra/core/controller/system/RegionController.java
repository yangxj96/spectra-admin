package com.devops00.spectra.core.controller.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.javabean.system.from.RegionPageFrom;
import com.devops00.spectra.core.javabean.system.vo.RegionPathVO;
import com.devops00.spectra.core.javabean.system.vo.RegionVO;
import com.devops00.spectra.core.service.system.RegionService;
import com.devops00.spectra.log.base.annotation.ULog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 行政区划相关接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 15:32
@RestController
@RequestMapping("/region")
public class RegionController {

    private final RegionService bindService;

    public RegionController(RegionService regionService) {
        this.bindService = regionService;
    }

    /// 懒加载树
    ///
    /// @param level 层级
    /// @param id    父级ID
    /// @return 根据条件获取的下级的列表
    @GetMapping("/lazy")
    public List<RegionVO> lazyTree(Integer level, @RequestParam(value = "id", required = false) String id) {
        return bindService.lazyTree(level, id);
    }

    @ULog("分页查询行政区划")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public IPage<RegionVO> page(PageFrom page, RegionPageFrom params) {
        return bindService.page(page, params);
    }

    @GetMapping("/path/{id}")
    public RegionPathVO getPath(@PathVariable UUID id) {
        return bindService.getPath(id);
    }

}
